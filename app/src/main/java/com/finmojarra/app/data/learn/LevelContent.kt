package com.finmojarra.app.data.learn

import androidx.annotation.StringRes
import com.finmojarra.app.R

/** Título y descripción de una de las dos tarjetas del final de la píldora. */
data class InfoCardContent(
    @StringRes val title: Int,
    @StringRes val description: Int
)

/**
 * Todo lo que cambia de un nivel a otro: el video destacado, la píldora, las dos tarjetas
 * de la píldora y la pregunta interactiva. Las pantallas son las mismas para todos.
 */
data class LevelContent(
    /** Id del curso al que pertenece (el mismo de [LearningPath.id]). */
    val pathId: String,
    @StringRes val videoTitle: Int,
    @StringRes val videoDescription: Int,
    @StringRes val authorMeta: Int,
    @StringRes val pillQuote: Int,
    /** Texto corto de la tarjeta de la píldora (antes de pulsar "Aprender"). */
    @StringRes val pillTeaser: Int,
    /** Texto completo de la cita dentro del detalle de la píldora. */
    @StringRes val pillFull: Int,
    @StringRes val pillCategory: Int,
    @StringRes val pillTitle: Int,
    /** Tarjeta de la izquierda (verde, icono de tarjeta). */
    val leftCard: InfoCardContent,
    /** Tarjeta de la derecha (amarilla, icono de advertencia). */
    val rightCard: InfoCardContent,
    val quiz: QuizQuestion
)

/** Niveles construidos. Para agregar otro: un [LevelContent] nuevo y su id en el catálogo. */
object LearningLevels {
    const val CREDIT_ID = "tarjeta-credito"
    const val PRACTICAL_ID = "intermedio-practico"

    val credit = LevelContent(
        pathId = CREDIT_ID,
        videoTitle = R.string.learn_video_title,
        videoDescription = R.string.learn_video_desc,
        authorMeta = R.string.learn_author_meta,
        pillQuote = R.string.learn_pill_quote,
        pillTeaser = R.string.learn_pill_teaser,
        pillFull = R.string.learn_pill_full,
        pillCategory = R.string.learn_pill_category,
        pillTitle = R.string.learn_pill_title,
        leftCard = InfoCardContent(R.string.learn_real_title, R.string.learn_real_desc),
        rightCard = InfoCardContent(R.string.learn_borrowed_title, R.string.learn_borrowed_desc),
        quiz = QuizQuestion(
            question = R.string.learn_quiz_question,
            options = listOf(R.string.learn_quiz_option_a, R.string.learn_quiz_option_b),
            correctIndex = 1,
            xp = 200
        )
    )

    val practical = LevelContent(
        pathId = PRACTICAL_ID,
        videoTitle = R.string.learn_l2_video_title,
        videoDescription = R.string.learn_l2_video_desc,
        authorMeta = R.string.learn_l2_author_meta,
        pillQuote = R.string.learn_l2_pill_quote,
        pillTeaser = R.string.learn_l2_pill_text,
        pillFull = R.string.learn_l2_pill_text,
        pillCategory = R.string.learn_l2_pill_category,
        pillTitle = R.string.learn_l2_pill_title,
        leftCard = InfoCardContent(R.string.learn_l2_card_cut_title, R.string.learn_l2_card_cut_desc),
        rightCard = InfoCardContent(R.string.learn_l2_card_due_title, R.string.learn_l2_card_due_desc),
        quiz = QuizQuestion(
            question = R.string.learn_l2_quiz_question,
            options = listOf(R.string.learn_l2_quiz_option_a, R.string.learn_l2_quiz_option_b),
            correctIndex = 1,
            xp = 200
        )
    )

    private val all = listOf(credit, practical).associateBy { it.pathId }

    /** El nivel de un curso; si el id no existe devuelve el primero. */
    fun byId(pathId: String): LevelContent = all[pathId] ?: credit
}
