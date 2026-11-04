package com.finmojarra.app.data.learn

import kotlinx.coroutines.flow.Flow

/** Avance del usuario en los cursos de "Vamos a aprender". */
interface LearningProgressRepository {
    /**
     * Pasos ya hechos de cada curso, por id del curso (por ejemplo
     * `{"tarjeta-credito": ["video"], "intermedio-practico": []}`). Se actualiza solo.
     */
    fun observeAllSteps(): Flow<Map<String, Set<String>>>

    /**
     * Preguntas del quiz ya respondidas bien, por id del curso. Las posiciones empiezan en 0
     * (por ejemplo `{"tarjeta-credito": [0, 2]}` = acertó la primera y la tercera).
     */
    fun observeQuizCorrect(): Flow<Map<String, Set<Int>>>

    /** Marca un paso como hecho (si ya estaba hecho, no pasa nada). */
    fun markStepDone(pathId: String, stepId: String)

    /**
     * Guarda un intento de respuesta del quiz (cuenta el intento y si fue correcto). Cuando
     * las [totalQuestions] preguntas ya están bien, marca también el paso "quiz" del curso.
     */
    fun recordQuizAnswer(pathId: String, questionIndex: Int, correct: Boolean, totalQuestions: Int)
}
