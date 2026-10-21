package com.finmojarra.app.data.goals

/** Meta de ahorro del usuario. Los montos están en pesos colombianos (COP). */
data class Goal(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double,
    /** Cuánto piensa ahorrar cada semana (0 si la meta es antigua y no lo tiene). */
    val weeklyAmount: Double = 0.0,
    /** Imagen de referencia (JPEG reducido en Base64) o null si la meta no tiene imagen. */
    val imageBase64: String? = null,
    /** Fecha límite en milisegundos (UTC). Ya no se pide en el formulario, pero se conserva. */
    val deadlineMillis: Long? = null,
    val createdAtMillis: Long? = null
) {
    /** Avance entre 0 y 1. */
    val progress: Float
        get() = if (targetAmount <= 0.0) 0f else (savedAmount / targetAmount).toFloat().coerceIn(0f, 1f)

    /** Lo que falta para llegar a la meta (nunca negativo). */
    val remainingAmount: Double
        get() = (targetAmount - savedAmount).coerceAtLeast(0.0)

    val isCompleted: Boolean get() = targetAmount > 0.0 && savedAmount >= targetAmount
}

sealed interface GoalsResult {
    /** Metas ordenadas de la más antigua a la más nueva (la posición + 1 es el "Meta N"). */
    data class Success(val goals: List<Goal>) : GoalsResult
    data object Failure : GoalsResult
}

/** Resultado de una escritura sobre una meta (crear, ingresar o retirar dinero). */
sealed interface GoalWriteResult {
    data object Success : GoalWriteResult
    data class Failure(val reason: Reason) : GoalWriteResult

    enum class Reason { NoSession, PermissionDenied, ImageFailed, Unknown }
}
