package com.finmojarra.app.data.account

/** Tipo de movimiento de la cuenta del usuario. [key] es el texto que se guarda en Firestore. */
enum class MovementType(val key: String) {
    Deposit("deposit"),
    Withdraw("withdraw");

    companion object {
        fun fromKey(key: String?): MovementType? = entries.firstOrNull { it.key == key }
    }
}

/** Un ingreso o un retiro de la cuenta. Los montos están en pesos colombianos (COP). */
data class Movement(
    val id: String,
    val type: MovementType,
    val amount: Double,
    /** Fecha en milisegundos (UTC); null solo un instante, mientras el servidor pone la hora. */
    val createdAtMillis: Long?
)

sealed interface AccountResult {
    /** [movements] viene del más reciente al más antiguo. */
    data class Success(val balance: Double, val movements: List<Movement>) : AccountResult
    data object Failure : AccountResult
}

/** Resultado de ingresar o retirar dinero. */
sealed interface AccountWriteResult {
    data object Success : AccountWriteResult
    data class Failure(val reason: Reason) : AccountWriteResult

    enum class Reason { NoSession, PermissionDenied, Unknown }
}
