package com.finmojarra.app.data.account

import kotlinx.coroutines.flow.Flow

/** ver el saldo y los movimientos, e ingresar o retirar dinero. */
interface AccountRepository {
    /** Emite el saldo y los últimos movimientos */
    fun observeAccount(): Flow<AccountResult>

    /** Ingresa o retira [amount] (siempre positivo) y deja el movimiento en el historial. */
    suspend fun move(type: MovementType, amount: Double): AccountWriteResult

    /**
     * Pasa dinero entre la cuenta y una meta
     * baja el saldo de la cuenta y sube lo ahorrado en la meta ( Double siempre positivo ) 
     */
    suspend fun transferToGoal(goalId: String, goalName: String, delta: Double): AccountWriteResult

    /**
     * Borra la meta y devuelve a la cuenta lo que tenía ahorrado ([savedAmount]), para que el
     * dinero no desaparezca.
     */
    suspend fun deleteGoalReturningMoney(goalId: String, goalName: String, savedAmount: Double): AccountWriteResult
}
