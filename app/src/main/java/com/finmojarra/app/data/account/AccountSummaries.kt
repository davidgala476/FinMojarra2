package com.finmojarra.app.data.account

import com.finmojarra.app.data.goals.Goal
import java.util.Calendar

/** Resumen de los movimientos del mes en curso. Los montos están en COP. */
data class MonthSummary(
    /** Lo que ingresaste a tu cuenta este mes. */
    val deposited: Double,
    /** Lo que retiraste de tu cuenta este mes. */
    val withdrawn: Double,
    /** Lo que pasó de tu cuenta a tus metas este mes (menos lo que volvió de ellas). */
    val toGoals: Double
)

/**
 * Ahorro semanal en metas: [saved] es lo que pasaste a tus metas desde el lunes y [target] lo
 * que te propusiste ahorrar por semana (la suma del aporte semanal de cada meta activa).
 */
data class WeeklySavings(
    val saved: Double,
    val target: Double
) {
    /** Lo que falta para cumplir el ahorro semanal (nunca negativo). */
    val missing: Double get() = (target - saved).coerceAtLeast(0.0)

    /** Hay al menos una meta activa con aporte semanal. */
    val hasTarget: Boolean get() = target > 0.0

    /** Avance entre 0 y 1. */
    val progress: Float
        get() = if (target <= 0.0) 0f else (saved / target).toFloat().coerceIn(0f, 1f)
}

/** Cálculos de periodos (semana y mes) con la zona horaria del teléfono. La semana empieza el lunes. */
object AccountSummaries {

    fun startOfWeek(now: Long = System.currentTimeMillis()): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val daysSinceMonday = (c.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
        c.add(Calendar.DAY_OF_YEAR, -daysSinceMonday)
        return c.timeInMillis
    }

    fun startOfMonth(now: Long = System.currentTimeMillis()): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    fun month(movements: List<Movement>, now: Long = System.currentTimeMillis()): MonthSummary {
        val since = startOfMonth(now)
        // Un movimiento recién hecho aún sin hora del servidor cuenta como de ahora.
        val inMonth = movements.filter { (it.createdAtMillis ?: now) >= since }
        fun total(type: MovementType) = inMonth.filter { it.type == type }.sumOf { it.amount }
        return MonthSummary(
            deposited = total(MovementType.Deposit),
            withdrawn = total(MovementType.Withdraw),
            toGoals = total(MovementType.GoalDeposit) - total(MovementType.GoalWithdraw)
        )
    }

    fun week(
        movements: List<Movement>,
        goals: List<Goal>,
        now: Long = System.currentTimeMillis()
    ): WeeklySavings {
        val since = startOfWeek(now)
        val inWeek = movements.filter { (it.createdAtMillis ?: now) >= since }
        val saved = inWeek.filter { it.type == MovementType.GoalDeposit }.sumOf { it.amount } -
            inWeek.filter { it.type == MovementType.GoalWithdraw }.sumOf { it.amount }
        // Una meta casi cumplida no necesita más de lo que le falta.
        val target = goals.filter { !it.isCompleted }
            .sumOf { minOf(it.weeklyAmount, it.remainingAmount) }
        return WeeklySavings(saved = saved.coerceAtLeast(0.0), target = target)
    }
}
