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
    val nextMilestoneXp: Int
) {
    /** Avance entre 0 y 1. */
    val progress: Float
        get() = if (totalLessons <= 0) 0f else (completedLessons.toFloat() / totalLessons).coerceIn(0f, 1f)

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
            id = "tarjeta-credito",
            title = R.string.learn_path_credit_title,
            badge = R.string.learn_path_credit_badge,
            completedLessons = 1,
            totalLessons = 5,
            nextMilestoneXp = 1_000
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
