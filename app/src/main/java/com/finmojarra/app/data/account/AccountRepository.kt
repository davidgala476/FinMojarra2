package com.finmojarra.app.data.account

import kotlinx.coroutines.flow.Flow

/** Contrato de la cuenta de ahorros: ver el saldo y los movimientos, e ingresar o retirar dinero. */
interface AccountRepository {
    /** Emite el saldo y los últimos movimientos; se actualiza solo cuando cambian en la nube. */
    fun observeAccount(): Flow<AccountResult>

    /** Ingresa o retira [amount] (siempre positivo) y deja el movimiento en el historial. */
    suspend fun move(type: MovementType, amount: Double): AccountWriteResult
}
