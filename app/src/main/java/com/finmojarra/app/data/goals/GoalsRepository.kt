package com.finmojarra.app.data.goals

import kotlinx.coroutines.flow.Flow

/**
 * Contrato de las metas. Parte 1: solo lectura. Crear, editar y mover dinero
 * (ingresar/retirar) se agregan en las siguientes partes.
 */
interface GoalsRepository {
    /** Emite la lista de metas del usuario y se actualiza sola cuando cambian en la nube. */
    fun observeGoals(): Flow<GoalsResult>
}
