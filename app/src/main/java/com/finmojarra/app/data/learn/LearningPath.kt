package com.finmojarra.app.data.learn

import androidx.annotation.StringRes
import com.finmojarra.app.R

/**
 * Ruta de aprendizaje de "Vamos a aprender": un curso con varias lecciones, el rango
 * (insignia) que se gana al terminarlo y el avance del usuario.
 */
data class LearningPath(
    val id: String,
    @StringRes val title: Int,
    /** Rango que se muestra en la insignia de la tarjeta ("Mojarra Junior"). */
    @StringRes val badge: Int,
    val completedLessons: Int,
    val totalLessons: Int,
    /** XP que faltan para el próximo hito. */
    val nextMilestoneXp: Int,
    /** true si el curso ya tiene su nivel construido (la tarjeta se puede abrir). */
    val hasContent: Boolean = false
) {
    /** Avance entre 0 y 1. */
    val progress: Float
        get() = if (totalLessons <= 0) 0f else (completedLessons.toFloat() / totalLessons).coerceIn(0f, 1f)

    /** Copia del curso con [steps] lecciones más marcadas como completadas (sin pasar del total). */
    fun withCompletedSteps(steps: Int): LearningPath =
        copy(completedLessons = (completedLessons + steps).coerceIn(0, totalLessons))

    /** Avance en porcentaje entero (1 de 5 = 20%, 1 de 3 = 33%). */
    val percent: Int
        get() = (progress * 100).toInt()
}

/**
 * Cursos disponibles. Por ahora son fijos (los del diseño de Figma); cuando el avance se
 * guarde por usuario, basta con leerlo de Firestore y construir esta misma lista.
 */
object LearningCatalog {
    val paths: List<LearningPath> = listOf(
        LearningPath(
            id = LearningLevels.CREDIT_ID,
            title = R.string.learn_path_credit_title,
            badge = R.string.learn_path_credit_badge,
            completedLessons = 0,
            totalLessons = 3,
            nextMilestoneXp = 1_000,
            hasContent = true
        ),
        LearningPath(
            id = LearningLevels.PRACTICAL_ID,
            title = R.string.learn_path_practical_title,
            badge = R.string.learn_path_practical_badge,
            completedLessons = 0,
            totalLessons = 3,
            nextMilestoneXp = 1_000,
            hasContent = true
        ),
        LearningPath(
            id = LearningLevels.STRATEGIC_ID,
            title = R.string.learn_path_strategic_title,
            badge = R.string.learn_path_strategic_badge,
            completedLessons = 0,
            totalLessons = 3,
            nextMilestoneXp = 1_000,
            hasContent = true
        )
    )
}

/** Pregunta de opción múltiple con XP de premio. */
data class QuizQuestion(
    @StringRes val question: Int,
    /** Textos (ids de strings) de las opciones, en orden. */
    val options: List<Int>,
    /** Posición (desde 0) de la opción correcta dentro de [options]. */
    val correctIndex: Int,
    val xp: Int
)

/** Pasos de cada nivel: cada uno que el usuario hace suma una lección completada. */
object LearningSteps {
    const val VIDEO = "video"
    const val QUIZ = "quiz"
    const val PILL = "pill"
    private val ALL = setOf(VIDEO, QUIZ, PILL)

    /** Cuántos de los pasos de un nivel aparecen en [steps]. */
    fun count(steps: Set<String>): Int = steps.count { it in ALL }
}
