package com.finmojarra.app.data.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/**
 * Datos extra del perfil guardados en la nube con Firestore.
 *
 * Estructura: colección `users`, un documento por usuario (el id es el UID de Firebase Auth):
 *   age, gender, occupation, education, phone, country, department, city, street,
 *   documentType, documentNumber, email, updatedAt,
 *   photoBase64 + photoUpdatedAt (la foto, reducida a ~480 px, guardada como texto).
 *
 * - Escucha el documento en tiempo real: lo que cambies en la consola o en otro dispositivo
 *   aparece en la app sin hacer nada.
 * - Firestore guarda una copia en el teléfono: sin internet, la app sigue mostrando los datos y
 *   los cambios se suben solos cuando vuelve la conexión.
 * - Para cambiar a Firebase Storage para la foto solo hay que modificar [savePhoto] y [removePhoto].
 */
class FirestoreProfileDetailsRepository(
    context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProfileDetailsRepository {

    private val appContext = context.applicationContext

    /** Un solo hilo para procesar las respuestas de Firestore en orden. */
    private val ioExecutor = Executors.newSingleThreadExecutor()

    private val _details = MutableStateFlow(ProfileDetails())
    override val details: StateFlow<ProfileDetails> = _details.asStateFlow()

    private var listener: ListenerRegistration? = null

    @Volatile
    private var listeningUid: String? = null

    init {
        // Al entrar o salir un usuario se empieza (o se deja) de escuchar su documento.
        auth.addAuthStateListener { reload() }
    }

    override fun reload() {
        val uid = auth.currentUser?.uid
        if (uid == listeningUid) return

        listener?.remove()
        listener = null
        listeningUid = uid
        _details.value = ProfileDetails()
        if (uid == null) return

        listener = userDoc(uid).addSnapshotListener(ioExecutor) { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "No se pudo leer el perfil de Firestore", error)
                return@addSnapshotListener
            }
            if (snapshot == null || listeningUid != uid) return@addSnapshotListener
            _details.value = snapshot.toDetails(uid)
        }
    }

    override fun save(transform: (ProfileDetails) -> ProfileDetails) {
        val uid = auth.currentUser?.uid ?: return
        val before = _details.value
        val after = transform(before)

        // Solo se suben los campos que cambiaron, para no pisar datos que aún no hayan cargado.
        val oldFields = before.toFields()
        val changes = after.toFields().filter { (field, value) -> oldFields[field] != value }
        if (changes.isEmpty()) return

        _details.value = after.copy(photoPath = before.photoPath, photoStamp = before.photoStamp)
        write(uid, changes)
    }

    override suspend fun savePhoto(source: Uri): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false
        try {
            val bytes = decodeAndCompress(source) ?: return@withContext false
            val stamp = System.currentTimeMillis()

            // Copia local para mostrarla al instante.
            val file = photoFile(uid)
            file.writeBytes(bytes)
            file.setLastModified(stamp)
            _details.update { it.copy(photoPath = file.absolutePath, photoStamp = stamp) }

            write(
                uid,
                mapOf(
                    "photoBase64" to Base64.encodeToString(bytes, Base64.NO_WRAP),
                    "photoUpdatedAt" to stamp
                )
            )
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "No se pudo guardar la foto", e)
            false
        }
    }

    override suspend fun removePhoto() {
        val uid = auth.currentUser?.uid ?: return
        withContext(Dispatchers.IO) { photoFile(uid).delete() }
        _details.update { it.copy(photoPath = "", photoStamp = 0L) }
        write(
            uid,
            mapOf(
                "photoBase64" to FieldValue.delete(),
                "photoUpdatedAt" to FieldValue.delete()
            )
        )
    }

    // ---------- Firestore ----------

    private fun userDoc(uid: String) = db.collection(USERS).document(uid)

    /**
     * No espera la respuesta del servidor (sin internet nunca llegaría): Firestore deja el
     * cambio en cola y lo sube cuando hay conexión.
     */
    private fun write(uid: String, data: Map<String, Any>) {
        val payload = data + mapOf(
            "email" to auth.currentUser?.email.orEmpty(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        userDoc(uid).set(payload, SetOptions.merge())
            .addOnFailureListener { Log.e(TAG, "No se pudo guardar en Firestore", it) }
    }

    private fun ProfileDetails.toFields(): Map<String, String> = mapOf(
        "age" to age,
        "gender" to gender,
        "occupation" to occupation,
        "education" to education,
        "phone" to phone,
        "country" to country,
        "department" to department,
        "city" to city,
        "street" to street,
        "documentType" to documentType,
        "documentNumber" to documentNumber
    )

    private fun DocumentSnapshot.toDetails(uid: String): ProfileDetails {
        fun s(field: String) = getString(field).orEmpty()

        val base64 = s("photoBase64")
        val stamp = getLong("photoUpdatedAt") ?: 0L
        val file = photoFile(uid)
        var photoPath = ""
        try {
            if (base64.isNotEmpty()) {
                val upToDate = file.exists() && stamp != 0L && abs(file.lastModified() - stamp) < 2000
                if (!upToDate) {
                    file.writeBytes(Base64.decode(base64, Base64.DEFAULT))
                    if (stamp != 0L) file.setLastModified(stamp)
                }
                photoPath = file.absolutePath
            } else if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo preparar la foto de perfil", e)
        }

        return ProfileDetails(
            age = s("age"),
            gender = s("gender"),
            occupation = s("occupation"),
            education = s("education"),
            phone = s("phone"),
            country = s("country"),
            department = s("department"),
            city = s("city"),
            street = s("street"),
            documentType = s("documentType"),
            documentNumber = s("documentNumber"),
            photoPath = photoPath,
            photoStamp = if (photoPath.isNotEmpty()) stamp else 0L
        )
    }

    // ---------- Foto ----------

    /** Copia de la foto de la nube (distinta de la del modo local para no mezclarlas). */
    private fun photoFile(uid: String) = File(appContext.filesDir, "profile_cloud_$uid.jpg")

    /** Lee la imagen elegida, la endereza, la reduce a [MAX_SIDE] px y la comprime a JPEG. */
    private fun decodeAndCompress(source: Uri): ByteArray? {
        val resolver = appContext.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > MAX_SIDE * 2 || bounds.outHeight / sample > MAX_SIDE * 2) {
            sample *= 2
        }

        val decoded = resolver.openInputStream(source)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null

        val orientation = resolver.openInputStream(source)?.use {
            ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        val matrix = Matrix()
        if (degrees != 0f) matrix.postRotate(degrees)
        val longest = max(decoded.width, decoded.height)
        if (longest > MAX_SIDE) {
            val scale = MAX_SIDE.toFloat() / longest
            matrix.postScale(scale, scale)
        }
        val bitmap = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)

        // Un documento de Firestore admite 1 MiB: se baja la calidad hasta que quepa con holgura.
        var quality = 85
        var bytes: ByteArray
        do {
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            bytes = out.toByteArray()
            quality -= 10
        } while (bytes.size > MAX_PHOTO_BYTES && quality >= 35)
        return bytes
    }

    private companion object {
        const val TAG = "FinMojarra"
        const val USERS = "users"
        const val MAX_SIDE = 480
        const val MAX_PHOTO_BYTES = 500_000
    }
}
