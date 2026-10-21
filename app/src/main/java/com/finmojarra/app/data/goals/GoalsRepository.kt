package com.finmojarra.app.data.goals

import kotlinx.coroutines.flow.Flow

/**
 * Contrato de las metas. Parte 1: lectura. Parte 2: crear. Ingresar y retirar dinero
 * se agregan en la parte 3.
 */
interface GoalsRepository {
    /** Emite la lista de metas del usuario y se actualiza sola cuando cambian en la nube. */
    fun observeGoals(): Flow<GoalsResult>

    /** Crea una meta nueva con 0 ahorrado. [deadlineMillis] es opcional (UTC). */
    suspend fun createGoal(
        name: String,
        targetAmount: Double,
        deadlineMillis: Long?
    ): CreateGoalResult
}
