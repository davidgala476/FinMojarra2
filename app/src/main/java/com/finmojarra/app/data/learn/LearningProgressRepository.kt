package com.finmojarra.app.data.learn

import kotlinx.coroutines.flow.Flow

/** Avance del usuario en los cursos de "Vamos a aprender". */
interface LearningProgressRepository {
    /**
     * Pasos ya hechos de cada curso, por id del curso (por ejemplo
     * `{"tarjeta-credito": ["video"], "intermedio-practico": []}`). Se actualiza solo.
     */
    fun observeAllSteps(): Flow<Map<String, Set<String>>>

    /** Marca un paso como hecho (si ya estaba hecho, no pasa nada). */
    fun markStepDone(pathId: String, stepId: String)
}
