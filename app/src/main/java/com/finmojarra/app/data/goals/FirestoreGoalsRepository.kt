package com.finmojarra.app.data.goals

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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

    private fun DocumentSnapshot.toGoal(): Goal? {
        val name = getString("name")?.takeIf { it.isNotBlank() } ?: return null
        return Goal(
            id = id,
            name = name,
            targetAmount = (get("targetAmount") as? Number)?.toDouble() ?: 0.0,
            savedAmount = (get("savedAmount") as? Number)?.toDouble() ?: 0.0,
            deadlineMillis = getTimestamp("deadline")?.toDate()?.time,
            createdAtMillis = getTimestamp("createdAt")?.toDate()?.time
        )
    }

    private companion object {
        const val TAG = "FinMojarra"
        const val USERS = "users"
        const val GOALS = "goals"
    }
}
