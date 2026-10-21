package com.finmojarra.app.data.goals

import android.util.Log
import com.finmojarra.app.data.auth.awaitResult
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException

/**
 * Metas guardadas en Firestore:  users/{uid}/goals/{goalId}
 *
 * Campos de cada meta:
 *   name (texto), targetAmount (número), savedAmount (número),
 *   deadline (Timestamp, opcional), createdAt (Timestamp, opcional)
 *
 * Escucha la subcolección en tiempo real; con Firestore sin internet muestra la copia local.
 */
class FirestoreGoalsRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : GoalsRepository {

    override fun observeGoals(): Flow<GoalsResult> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(GoalsResult.Failure)
            close()
            return@callbackFlow
        }

        val registration = db.collection(USERS).document(uid).collection(GOALS)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e(TAG, "No se pudieron leer las metas", error)
                    trySend(GoalsResult.Failure)
                    return@addSnapshotListener
                }
                val goals = snapshot.documents
                    .mapNotNull { it.toGoal() }
                    // Las más nuevas primero; las que no tienen fecha de creación, al final.
                    .sortedWith(
                        compareByDescending<Goal> { it.createdAtMillis ?: Long.MIN_VALUE }
                            .thenBy { it.name.lowercase() }
                    )
                trySend(GoalsResult.Success(goals))
            }

        awaitClose { registration.remove() }
    }

    override suspend fun createGoal(
        name: String,
        targetAmount: Double,
        deadlineMillis: Long?
    ): CreateGoalResult {
        val uid = auth.currentUser?.uid
            ?: return CreateGoalResult.Failure(CreateGoalResult.Reason.NoSession)
        val ref = db.collection(USERS).document(uid).collection(GOALS).document()
        val data = mutableMapOf<String, Any>(
            "name" to name.trim(),
            "targetAmount" to targetAmount,
            "savedAmount" to 0.0,
            "createdAt" to FieldValue.serverTimestamp()
        )
        deadlineMillis?.let { data["deadline"] = Timestamp(Date(it)) }

        return try {
            // Sin internet Firestore nunca responde: tras el tiempo de espera se da por buena,
            // porque la meta queda en cola y se sube sola al volver la conexión. Los errores
            // reales (por ejemplo, reglas que no lo permiten) sí llegan antes.
            withTimeoutOrNull(WRITE_TIMEOUT_MS) { ref.set(data).awaitResult() }
            CreateGoalResult.Success
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "No se pudo crear la meta", e)
            val denied = e is FirebaseFirestoreException &&
                e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            CreateGoalResult.Failure(
                if (denied) CreateGoalResult.Reason.PermissionDenied
                else CreateGoalResult.Reason.Unknown
            )
        }
    }

    private fun DocumentSnapshot.toGoal(): Goal? {
        val name = getString("name")?.takeIf { it.isNotBlank() } ?: return null
        return Goal(
            id = id,
            name = name,
            targetAmount = (get("targetAmount") as? Number)?.toDouble() ?: 0.0,
            savedAmount = (get("savedAmount") as? Number)?.toDouble() ?: 0.0,
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
