package com.finmojarra.app.data.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.media.ExifInterface
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/** Datos adicionales del perfil (los que no guarda Firebase Authentication). */
data class ProfileDetails(
    val age: String = "",
    val gender: String = "",
    val occupation: String = "",
    val education: String = "",
    val phone: String = "",
    val country: String = "",
    val department: String = "",
    val city: String = "",
    /** Dirección de la calle, por ejemplo "Av calle 80 # 80 - 80". */
    val street: String = "",
    val documentType: String = "",
    val documentNumber: String = "",
    /** Ruta del archivo de la foto de perfil, o vacío si no hay. */
    val photoPath: String = "",
    /** Cambia cada vez que se reemplaza la foto (para que la pantalla la vuelva a cargar). */
    val photoStamp: Long = 0L
) {
    /** "Bogotá, Bogotá DC" (ciudad, departamento) como en el diseño del perfil. */
    val locationLabel: String
        get() = listOf(city, department).filter { it.isNotBlank() }.joinToString(", ")

    /** "CC 65 583430" (tipo + número). */
    val documentLabel: String
        get() = listOf(documentType, documentNumber).filter { it.isNotBlank() }.joinToString(" ")
}

/** Contrato para leer/guardar los datos extra del perfil y la foto. */
interface ProfileDetailsRepository {
    val details: StateFlow<ProfileDetails>

    /** Vuelve a leer los datos del usuario que tiene la sesión activa. */
    fun reload()

    fun save(transform: (ProfileDetails) -> ProfileDetails)

    /** Copia la imagen elegida (galería o cámara) como foto de perfil. */
    suspend fun savePhoto(source: Uri): Boolean

    suspend fun removePhoto()
}

/**
 * Guarda los datos en el teléfono (SharedPreferences), separados por usuario.
 * Cuando se agregue Firestore, solo hay que crear otra implementación de
 * [ProfileDetailsRepository] y cambiarla en AppContainer.
 */
class LocalProfileDetailsRepository(
    context: Context
) : ProfileDetailsRepository {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("fin_profile_details", Context.MODE_PRIVATE)

    private val _details = MutableStateFlow(read())
    override val details: StateFlow<ProfileDetails> = _details.asStateFlow()

    private fun uid(): String = FirebaseAuth.getInstance().currentUser?.uid ?: "anon"

    private fun key(field: String) = "${uid()}_$field"

    private fun photoFile() = File(appContext.filesDir, "profile_${uid()}.jpg")

    private fun read(): ProfileDetails {
        fun s(field: String) = prefs.getString(key(field), "").orEmpty()
        val photo = photoFile()
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
            photoPath = if (photo.exists()) photo.absolutePath else "",
            photoStamp = if (photo.exists()) photo.lastModified() else 0L
        )
    }

    override fun reload() {
        _details.value = read()
    }

    override fun save(transform: (ProfileDetails) -> ProfileDetails) {
        val updated = transform(read())
        prefs.edit()
            .putString(key("age"), updated.age)
            .putString(key("gender"), updated.gender)
            .putString(key("occupation"), updated.occupation)
            .putString(key("education"), updated.education)
            .putString(key("phone"), updated.phone)
            .putString(key("country"), updated.country)
            .putString(key("department"), updated.department)
            .putString(key("city"), updated.city)
            .putString(key("street"), updated.street)
            .putString(key("documentType"), updated.documentType)
            .putString(key("documentNumber"), updated.documentNumber)
            .apply()
        _details.value = read()
    }

    override suspend fun savePhoto(source: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val resolver = appContext.contentResolver

            // 1) Leer solo las medidas para reducir la imagen al decodificarla.
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (bounds.outWidth / sample > MAX_SIDE * 2 || bounds.outHeight / sample > MAX_SIDE * 2) {
                sample *= 2
            }

            // 2) Decodificar la imagen.
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            val decoded = resolver.openInputStream(source)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return@withContext false

            // 3) Corregir la rotación que guardan las cámaras (EXIF).
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
            val bitmap = if (degrees != 0f) {
                val matrix = Matrix().apply { postRotate(degrees) }
                Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            } else {
                decoded
            }

            // 4) Guardar como JPEG dentro del almacenamiento privado de la app.
            FileOutputStream(photoFile()).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            reload()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun removePhoto() {
        withContext(Dispatchers.IO) { photoFile().delete() }
        reload()
    }

    private companion object {
        const val MAX_SIDE = 1024
    }
}
