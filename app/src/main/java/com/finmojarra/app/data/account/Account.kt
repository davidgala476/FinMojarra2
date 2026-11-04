package com.finmojarra.app.data.account

/** Tipo de movimiento (Define los movimientos con una lista cerrada) . [key] es el texto que se guarda en Firestore. */
enum class MovementType(val key: String) {
    Deposit("deposit"),
    Withdraw("withdraw"),
    /** Dinero que pasa de la cuenta a una meta (baja el saldo). */
    GoalDeposit("goal_deposit"),
    /** Dinero que vuelve de una meta a la cuenta (sube el saldo). */
    GoalWithdraw("goal_withdraw");

    /** true si el movimiento suma al saldo de la cuenta; false si lo resta.(Es una propiedad) */
    val addsToBalance: Boolean
        get() = this == Deposit || this == GoalWithdraw

    companion object {
        fun fromKey(key: String?): MovementType? = entries.firstOrNull { it.key == key }
    }
}

/** Un ingreso o un retiro de la cuenta. Los montos están en pesos . */
data class Movement(
    val id: String,
    val type: MovementType,
    val amount: Double,
    /** Nombre de la meta, solo en los movimientos entre la cuenta y una meta. */
    val goalName: String? = null,
    /** Fecha   null solo un instante, mientras el servidor pone la hora. */
    val createdAtMillis: Long?
)

sealed interface AccountResult {
    /** [movements] viene del más reciente al más antiguo. */
    data class Success(val balance: Double, val movements: List<Movement>) : AccountResult
    data object Failure : AccountResult
}

/** Resultado de ingresar o retirar dinero. (Muestra el resultado con los posibles errores o si se hizo de manera correcta ) */
sealed interface AccountWriteResult {
    data object Success : AccountWriteResult
    data class Failure(val reason: Reason) : AccountWriteResult

    enum class Reason { NoSession, PermissionDenied, Unknown }
}
