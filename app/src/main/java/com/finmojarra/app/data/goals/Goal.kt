package com.finmojarra.app.data.goals

/** Meta de ahorro del usuario. Los montos están en pesos colombianos (COP). */
data class Goal(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double,
    /** Fecha límite en milisegundos (UTC), o null si la meta no tiene fecha. */
    val deadlineMillis: Long? = null,
    val createdAtMillis: Long? = null
) {
    /** Avance entre 0 y 1. */
    val progress: Float
        get() = if (targetAmount <= 0.0) 0f else (savedAmount / targetAmount).toFloat().coerceIn(0f, 1f)

    val isCompleted: Boolean get() = targetAmount > 0.0 && savedAmount >= targetAmount
}

sealed interface GoalsResult {
    data class Success(val goals: List<Goal>) : GoalsResult
    data object Failure : GoalsResult
}

/** Resultado de crear una meta. */
sealed interface CreateGoalResult {
    data object Success : CreateGoalResult
    data class Failure(val reason: Reason) : CreateGoalResult

    enum class Reason { NoSession, PermissionDenied, Unknown }
}
