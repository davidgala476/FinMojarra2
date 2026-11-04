package com.finmojarra.app.ui.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finmojarra.app.R
import com.finmojarra.app.data.learn.LevelContent
import com.finmojarra.app.data.learn.QuizQuestion
import com.finmojarra.app.ui.theme.FinError
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinSuccess
import com.finmojarra.app.ui.theme.FinTeal
import com.finmojarra.app.ui.theme.FinWhite

private enum class OptionState { Idle, Wrong, Right }

/**
 * "Píldoras en un minuto": una píldora para leer (botón Aprender) y las 3 preguntas
 * interactivas del nivel. Al elegir una opción se muestra "Respuesta correcta" (verde) o
 * "Respuesta incorrecta" (rojo); si te equivocas puedes intentar con otra opción.
 *
 * Cada intento se guarda en Firebase ([onAnswer]). Las preguntas ya acertadas
 * ([correctQuestions], leídas de Firebase) quedan bloqueadas con la opción correcta en
 * verde, aunque salgas de la pantalla o cierres la app. [quizDone] (quiz completo, también
 * de Firebase) bloquea las tres; así los usuarios que ya habían terminado el quiz antiguo
 * de una sola pregunta no lo pierden.
 */
@Composable
fun PillsScreen(
    level: LevelContent,
    photoPath: String,
    photoStamp: Long,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenGoals: () -> Unit,
    onLearn: () -> Unit,
    quizDone: Boolean,
    correctQuestions: Set<Int>,
    onAnswer: (questionIndex: Int, correct: Boolean) -> Unit
) {
    val questions = level.quiz
    // Opción elegida en esta sesión por pregunta (-1 = ninguna). Se reinicia por nivel.
    var selected by rememberSaveable(level.pathId) { mutableStateOf(IntArray(questions.size) { -1 }) }

    val correctCount = questions.indices.count { i ->
        quizDone || i in correctQuestions || selected[i] == questions[i].correctIndex
    }

    LearnFrame(
        photoPath = photoPath,
        photoStamp = photoStamp,
        onBack = onBack,
        onOpenProfile = onOpenProfile,
        onOpenSavings = onOpenSavings,
        onOpenGoals = onOpenGoals
    ) {
        LearnSectionTitle(title = stringResource(R.string.learn_pills_title))
        PillTeaserCard(level = level, onLearn = onLearn)

        Text(
            text = stringResource(R.string.learn_quiz_progress, correctCount, questions.size),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            ),
            color = FinTeal,
            modifier = Modifier.padding(start = 8.dp)
        )

        questions.forEachIndexed { index, quiz ->
            // Bloqueada si ya estaba guardada como correcta o si se acaba de acertar.
            val alreadyDone = quizDone || index in correctQuestions
            val answeredCorrectly = alreadyDone || selected[index] == quiz.correctIndex
            // Si ya estaba resuelta de antes, se muestra la opción correcta marcada.
            val shownSelection = if (alreadyDone) quiz.correctIndex else selected[index].takeIf { it >= 0 }

            QuizCard(
                quiz = quiz,
                number = index + 1,
                total = questions.size,
                selected = shownSelection,
                locked = answeredCorrectly,
                onSelect = { option ->
                    if (!answeredCorrectly) {
                        selected = selected.copyOf().also { it[index] = option }
                        onAnswer(index, option == quiz.correctIndex)
                    }
                }
            )

            shownSelection?.let {
                Text(
                    text = stringResource(
                        if (answeredCorrectly) R.string.learn_quiz_correct else R.string.learn_quiz_incorrect
                    ),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = if (answeredCorrectly) FinSuccess else FinError,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PillTeaserCard(level: LevelContent, onLearn: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .learnCard()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(level.pillQuote),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            ),
            color = FinInk
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(level.pillTeaser),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = com.finmojarra.app.ui.theme.FinMutedText
        )
        Spacer(modifier = Modifier.height(12.dp))
        TealPillButton(
            text = stringResource(R.string.learn_learn_button),
            onClick = onLearn,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun QuizCard(
    quiz: QuizQuestion,
    number: Int,
    total: Int,
    selected: Int?,
    locked: Boolean,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .learnCard()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.learn_quiz_counter, number, total),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = FinInk
            )
            Text(
                text = stringResource(R.string.learn_quiz_xp, quiz.xp),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = FinTeal
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(quiz.question),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = FinInk
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.learn_quiz_options),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            ),
            color = FinInk
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            quiz.options.forEachIndexed { index, optionText ->
                val state = when {
                    selected != index -> OptionState.Idle
                    index == quiz.correctIndex -> OptionState.Right
                    else -> OptionState.Wrong
                }
                OptionButton(
                    text = stringResource(optionText),
                    state = state,
                    enabled = !locked,
                    onClick = { onSelect(index) }
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun OptionButton(
    text: String,
    state: OptionState,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val color = when (state) {
        OptionState.Idle -> FinInk
        OptionState.Wrong -> FinError
        OptionState.Right -> FinSuccess
    }
    val fill = if (state == OptionState.Idle) FinWhite else color.copy(alpha = 0.08f)
    val shape = RoundedCornerShape(8.dp)
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = color,
        modifier = Modifier
            .clip(shape)
            .background(fill)
            .border(1.dp, if (state == OptionState.Idle) Color(0xFF1F1F1F) else color, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    )
}
