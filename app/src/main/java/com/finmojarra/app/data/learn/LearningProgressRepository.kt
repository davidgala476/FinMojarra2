package com.finmojarra.app.data.learn

import kotlinx.coroutines.flow.Flow

/** Avance del usuario en los cursos de "Vamos a aprender". */
interface LearningProgressRepository {
    /** Ids de los pasos ya hechos en el curso [pathId]; se actualiza solo al cambiar. */
    fun observeSteps(pathId: String): Flow<Set<String>>

    /** Marca un paso como hecho (si ya estaba hecho, no pasa nada). */
    fun markStepDone(pathId: String, stepId: String)
}
