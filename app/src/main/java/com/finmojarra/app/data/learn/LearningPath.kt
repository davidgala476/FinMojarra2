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
    /** true si el curso ya tiene su primer nivel construido (la tarjeta se puede abrir). */
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
            id = CreditLevelContent.PATH_ID,
            title = R.string.learn_path_credit_title,
            badge = R.string.learn_path_credit_badge,
            completedLessons = 0,
            totalLessons = 3,
            nextMilestoneXp = 1_000,
            hasContent = true
        ),
        LearningPath(
            id = "intermedio-practico",
            title = R.string.learn_path_practical_title,
            badge = R.string.learn_path_practical_badge,
            completedLessons = 1,
            totalLessons = 3,
            nextMilestoneXp = 1_000
        ),
        LearningPath(
            id = "estrategico",
            title = R.string.learn_path_strategic_title,
            badge = R.string.learn_path_strategic_badge,
            completedLessons = 1,
            totalLessons = 3,
            nextMilestoneXp = 1_000
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

/** Contenido del primer nivel de "Tarjeta de crédito sin miedo". */
object CreditLevelContent {
    const val PATH_ID = "tarjeta-credito"

    /** Pasos del nivel; cada uno que el usuario hace suma una lección completada. */
    const val STEP_VIDEO = "video"
    const val STEP_QUIZ = "quiz"
    const val STEP_PILL = "pill"
    private val ALL_STEPS = setOf(STEP_VIDEO, STEP_QUIZ, STEP_PILL)

    /** Cuántos de los pasos del nivel aparecen en [steps]. */
    fun doneCount(steps: Set<String>): Int = steps.count { it in ALL_STEPS }

    val quiz = QuizQuestion(
        question = R.string.learn_quiz_question,
        options = listOf(R.string.learn_quiz_option_a, R.string.learn_quiz_option_b),
        correctIndex = 1,
        xp = 200
    )
}
