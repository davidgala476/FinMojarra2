package com.finmojarra.app.data.learn

import androidx.annotation.StringRes
import com.finmojarra.app.R

/**
 * Nivel de aprendizaje del usuario, calculado con lo que hizo en "Vamos a aprender".
 *
 * - [level]: cursos terminados (video, píldora y quiz completo). 0 = recién empieza, 3 = todos.
 * - [rank]: insignia que ganó con el último curso terminado ("Mojarra Junior"...).
 * - [nextRank]: insignia del siguiente curso, o null si ya los terminó todos.
 * - [completedSteps] / [totalSteps]: pasos hechos entre todos los cursos (video, píldora, quiz).
 * - [xp]: suma del XP de las preguntas del quiz que acertó.
 */
data class LearningLevel(
    val level: Int,
    val maxLevel: Int,
    @StringRes val rank: Int,
    @StringRes val nextRank: Int?,
    val completedSteps: Int,
    val totalSteps: Int,
    val xp: Int
) {
    /** Avance total entre 0 y 1. */
    val progress: Float
        get() = if (totalSteps <= 0) 0f else (completedSteps.toFloat() / totalSteps).coerceIn(0f, 1f)

    companion object {
        /** Calcula el nivel con los pasos hechos y las preguntas acertadas de cada curso. */
        fun from(
            stepsByPath: Map<String, Set<String>>,
            quizCorrectByPath: Map<String, Set<Int>>
        ): LearningLevel {
            val paths = LearningCatalog.paths
            val finished = paths.count { path ->
                LearningSteps.count(stepsByPath[path.id].orEmpty()) >= LearningSteps.TOTAL
            }
            val doneSteps = paths.sumOf { path ->
                LearningSteps.count(stepsByPath[path.id].orEmpty())
            }
            val xp = paths.sumOf { path ->
                val questions = LearningLevels.byId(path.id).quiz
                quizCorrectByPath[path.id].orEmpty().sumOf { index -> questions.getOrNull(index)?.xp ?: 0 }
            }
            return LearningLevel(
                level = finished,
                maxLevel = paths.size,
                rank = if (finished == 0) R.string.learn_level_start else paths[finished - 1].badge,
                nextRank = paths.getOrNull(finished)?.badge,
                completedSteps = doneSteps,
                totalSteps = paths.size * LearningSteps.TOTAL,
                xp = xp
            )
        }
    }
}
