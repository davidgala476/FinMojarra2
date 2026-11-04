package com.finmojarra.app.data.account

import kotlinx.coroutines.flow.Flow

/** Contrato de la cuenta de ahorros: ver el saldo y los movimientos, e ingresar o retirar dinero. */
interface AccountRepository {
    /** Emite el saldo y los últimos movimientos; se actualiza solo cuando cambian en la nube. */
    fun observeAccount(): Flow<AccountResult>

    /** Ingresa o retira [amount] (siempre positivo) y deja el movimiento en el historial. */
    suspend fun move(type: MovementType, amount: Double): AccountWriteResult

    /**
     * Pasa dinero entre la cuenta y una meta, todo en una sola escritura: con [delta] positivo
     * baja el saldo de la cuenta y sube lo ahorrado en la meta; con [delta] negativo, al revés.
     */
    suspend fun transferToGoal(goalId: String, goalName: String, delta: Double): AccountWriteResult

    /**
     * Borra la meta y devuelve a la cuenta lo que tenía ahorrado ([savedAmount]), para que el
     * dinero no desaparezca.
     */
    suspend fun deleteGoalReturningMoney(goalId: String, goalName: String, savedAmount: Double): AccountWriteResult
}
