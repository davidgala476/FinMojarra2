package com.finmojarra.app.data.goals

import android.content.Context
import android.net.Uri
import android.util.Log
import com.finmojarra.app.data.auth.awaitResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException

/**
 * Metas guardadas en Firestore:  users/{uid}/goals/{goalId}
 *
 * Campos de cada meta:
 *   name (texto), targetAmount (número), savedAmount (número), weeklyAmount (número),
 *   imageBase64 (texto, opcional: la imagen de referencia reducida), createdAt (Timestamp),
 *   deadline (Timestamp, opcional, solo metas antiguas)
 *
 * Escucha la subcolección en tiempo real; con Firestore sin internet muestra la copia local.
 */
class FirestoreGoalsRepository(
    context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : GoalsRepository {

    private val appContext = context.applicationContext

    override fun observeGoals(): Flow<GoalsResult> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(GoalsResult.Failure)
            close()
            return@callbackFlow
        }

        val registration = goalsCollection(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e(TAG, "No se pudieron leer las metas", error)
                    trySend(GoalsResult.Failure)
                    return@addSnapshotListener
                }
                val goals = snapshot.documents
                    .mapNotNull { it.toGoal() }
                    // La más antigua primero: su posición es el "Meta 1", "Meta 2"...
                    .sortedWith(
                        compareBy<Goal> { it.createdAtMillis ?: Long.MAX_VALUE }
                            .thenBy { it.name.lowercase() }
                    )
                trySend(GoalsResult.Success(goals))
            }

        awaitClose { registration.remove() }
    }

    override suspend fun createGoal(
        name: String,
        targetAmount: Double,
        weeklyAmount: Double,
        imageUri: Uri?
    ): GoalWriteResult {
        val uid = auth.currentUser?.uid
            ?: return GoalWriteResult.Failure(GoalWriteResult.Reason.NoSession)

        val data = mutableMapOf<String, Any>(
            "name" to name.trim(),
            "targetAmount" to targetAmount,
            "savedAmount" to 0.0,
            "weeklyAmount" to weeklyAmount,
            "createdAt" to FieldValue.serverTimestamp()
        )

        if (imageUri != null) {
            val base64 = try {
                withContext(Dispatchers.IO) { GoalImageCodec.toBase64(appContext, imageUri) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "No se pudo procesar la imagen de la meta", e)
                null
            } ?: return GoalWriteResult.Failure(GoalWriteResult.Reason.ImageFailed)
            data["imageBase64"] = base64
        }

        val ref = goalsCollection(uid).document()
        return write { ref.set(data).awaitResult() }
    }

    override suspend fun addToSaved(goalId: String, delta: Double): GoalWriteResult {
        val uid = auth.currentUser?.uid
            ?: return GoalWriteResult.Failure(GoalWriteResult.Reason.NoSession)
        val ref = goalsCollection(uid).document(goalId)
        // increment() suma en el servidor, así que dos dispositivos no se pisan el saldo.
        return write { ref.update("savedAmount", FieldValue.increment(delta)).awaitResult() }
    }

    /**
     * Sin internet Firestore nunca responde: tras el tiempo de espera se da por buena, porque
     * el cambio queda en cola y se sube solo al volver la conexión. Los errores reales
     * (por ejemplo, reglas que no lo permiten) sí llegan antes.
     */
    private suspend fun write(block: suspend () -> Unit): GoalWriteResult = try {
        withTimeoutOrNull(WRITE_TIMEOUT_MS) { block() }
        GoalWriteResult.Success
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Log.e(TAG, "No se pudo guardar la meta", e)
        val denied = e is FirebaseFirestoreException &&
            e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
        GoalWriteResult.Failure(
            if (denied) GoalWriteResult.Reason.PermissionDenied else GoalWriteResult.Reason.Unknown
        )
    }

    private fun goalsCollection(uid: String) =
        db.collection(USERS).document(uid).collection(GOALS)

    private fun DocumentSnapshot.toGoal(): Goal? {
        val name = getString("name")?.takeIf { it.isNotBlank() } ?: return null
        return Goal(
            id = id,
            name = name,
            targetAmount = (get("targetAmount") as? Number)?.toDouble() ?: 0.0,
            savedAmount = (get("savedAmount") as? Number)?.toDouble() ?: 0.0,
            weeklyAmount = (get("weeklyAmount") as? Number)?.toDouble() ?: 0.0,
            imageBase64 = getString("imageBase64")?.takeIf { it.isNotEmpty() },
            deadlineMillis = getTimestamp("deadline")?.toDate()?.time,
            createdAtMillis = getTimestamp(
                "createdAt",
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )?.toDate()?.time
        )
    }

    private companion object {
        const val TAG = "FinMojarra"
        const val USERS = "users"
        const val GOALS = "goals"
        const val WRITE_TIMEOUT_MS = 4_000L
    }
}
