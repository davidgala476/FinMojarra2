package com.finmojarra.app.data.learn

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

/**
 * Avance de aprendizaje guardado en Firestore:  users/{uid}/learning/{pathId} (un documento por curso)
 *
 * Campos del documento:
 *  - steps: lista con los pasos hechos, por ejemplo ["video", "pill", "quiz"].
 *  - completedLessons / totalLessons / percent: el avance del nivel ya calculado (para verlo fácil en la consola).
 *  - quiz: mapa con una entrada por pregunta ("q1", "q2", "q3"), cada una con
 *      correct (true si ya la acertó), attempts (cuántos intentos lleva) y answeredAt (fecha del último intento).
 *  - updatedAt: fecha de la última vez que cambió algo.
 *
 * Todo se escribe con merge, así que nunca se pisan datos de otras partes del documento.
 */
class FirestoreLearningProgressRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : LearningProgressRepository {

    /** Última copia de los documentos recibida; sirve para calcular el avance al escribir. */
    @Volatile
    private var cache: Map<String, DocumentSnapshot> = emptyMap()

    private fun observeDocs(): Flow<List<DocumentSnapshot>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = learningCollection(uid).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                Log.e(TAG, "No se pudo leer el avance de aprendizaje", error)
                return@addSnapshotListener
            }
            cache = snapshot.documents.associateBy { it.id }
            trySend(snapshot.documents)
        }

        awaitClose { registration.remove() }
    }

    override fun observeAllSteps(): Flow<Map<String, Set<String>>> =
        observeDocs().map { docs -> docs.associate { it.id to stepsOf(it) } }

    override fun observeQuizCorrect(): Flow<Map<String, Set<Int>>> =
        observeDocs().map { docs -> docs.associate { it.id to quizCorrectOf(it) } }

    override fun markStepDone(pathId: String, stepId: String) {
        val uid = auth.currentUser?.uid ?: return
        val steps = stepsOf(cache[pathId]) + stepId
        // Sin internet queda en cola y se sube solo; el listener local ya ve el cambio.
        progressDoc(uid, pathId)
            .set(stepsFields(steps), SetOptions.merge())
            .addOnFailureListener { Log.e(TAG, "No se pudo guardar el avance de aprendizaje", it) }
    }

    override fun recordQuizAnswer(
        pathId: String,
        questionIndex: Int,
        correct: Boolean,
        totalQuestions: Int
    ) {
        val uid = auth.currentUser?.uid ?: return
        val doc = cache[pathId]

        val fields = mutableMapOf<String, Any>(
            "quiz" to mapOf(
                "q${questionIndex + 1}" to mapOf(
                    "correct" to correct,
                    "attempts" to FieldValue.increment(1),
                    "answeredAt" to FieldValue.serverTimestamp()
                )
            ),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        // Si con esta respuesta ya están todas bien, el paso "quiz" del nivel queda hecho.
        val allCorrect = correct && (quizCorrectOf(doc) + questionIndex).size >= totalQuestions
        if (allCorrect) {
            fields += stepsFields(stepsOf(doc) + LearningSteps.QUIZ)
        }

        progressDoc(uid, pathId)
            .set(fields, SetOptions.merge())
            .addOnFailureListener { Log.e(TAG, "No se pudo guardar la respuesta del quiz", it) }
    }

    /** Campos que describen el avance del nivel para el conjunto de pasos [steps]. */
    private fun stepsFields(steps: Set<String>): Map<String, Any> {
        val done = LearningSteps.count(steps)
        val total = LearningSteps.TOTAL
        return mapOf(
            "steps" to FieldValue.arrayUnion(*steps.toTypedArray()),
            "completedLessons" to done,
            "totalLessons" to total,
            "percent" to (done * 100 / total),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    private fun stepsOf(doc: DocumentSnapshot?): Set<String> =
        (doc?.get("steps") as? List<*>)?.filterIsInstance<String>()?.toSet().orEmpty()

    private fun quizCorrectOf(doc: DocumentSnapshot?): Set<Int> {
        val quiz = doc?.get("quiz") as? Map<*, *> ?: return emptySet()
        return quiz.mapNotNull { (key, value) ->
            val index = (key as? String)?.removePrefix("q")?.toIntOrNull()?.minus(1)
            val correct = (value as? Map<*, *>)?.get("correct") as? Boolean
            if (index != null && correct == true) index else null
        }.toSet()
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
