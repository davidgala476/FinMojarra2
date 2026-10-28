package com.finmojarra.app.data.learn

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Avance de aprendizaje guardado en Firestore:  users/{uid}/learning/{pathId} (un documento por curso)
 *
 * Campo: steps (lista de textos con los pasos hechos, por ejemplo ["video", "quiz"]).
 * Usa arrayUnion, así que marcar el mismo paso dos veces no lo repite.
 */
class FirestoreLearningProgressRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : LearningProgressRepository {

    override fun observeAllSteps(): Flow<Map<String, Set<String>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyMap())
            close()
            return@callbackFlow
        }

        val registration = learningCollection(uid).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                Log.e(TAG, "No se pudo leer el avance de aprendizaje", error)
                return@addSnapshotListener
            }
            val progress = snapshot.documents.associate { doc ->
                doc.id to (doc.get("steps") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.toSet()
                    .orEmpty()
            }
            trySend(progress)
        }

        awaitClose { registration.remove() }
    }

    override fun markStepDone(pathId: String, stepId: String) {
        val uid = auth.currentUser?.uid ?: return
        // Sin internet queda en cola y se sube solo; el listener local ya ve el cambio.
        progressDoc(uid, pathId)
            .set(mapOf("steps" to FieldValue.arrayUnion(stepId)), SetOptions.merge())
            .addOnFailureListener { Log.e(TAG, "No se pudo guardar el avance de aprendizaje", it) }
    }

    private fun learningCollection(uid: String) =
        db.collection(USERS).document(uid).collection(LEARNING)

    private fun progressDoc(uid: String, pathId: String) =
        learningCollection(uid).document(pathId)

    private companion object {
        const val TAG = "FinMojarra"
        const val USERS = "users"
        const val LEARNING = "learning"
    }
}
