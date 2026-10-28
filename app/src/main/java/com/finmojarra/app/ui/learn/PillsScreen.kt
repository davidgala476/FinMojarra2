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
import com.finmojarra.app.data.learn.CreditLevelContent
import com.finmojarra.app.data.learn.QuizQuestion
import com.finmojarra.app.ui.theme.FinError
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinSuccess
import com.finmojarra.app.ui.theme.FinTeal
import com.finmojarra.app.ui.theme.FinWhite

private enum class OptionState { Idle, Wrong, Right }

/**
 * "Píldoras en un minuto": una píldora para leer (botón Aprender) y la pregunta interactiva.
 * Al elegir una opción se muestra "Respuesta correcta" (verde) o "Respuesta incorrecta"
 * (rojo); si te equivocas puedes intentar con otra opción, las veces que quieras.
 *
 * Cuando la pregunta ya se respondió bien ([quizDone], dato guardado en Firebase) queda
 * bloqueada: se muestra la opción correcta en verde y no se puede volver a responder,
 * ni siquiera después de salir de la pantalla o cerrar la app.
 */
@Composable
fun PillsScreen(
    photoPath: String,
    photoStamp: Long,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenGoals: () -> Unit,
    onLearn: () -> Unit,
    quizDone: Boolean,
    onCorrectAnswer: () -> Unit
) {
    val quiz = CreditLevelContent.quiz
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    // Bloqueada si ya estaba guardada como correcta o si se acaba de acertar.
    val answeredCorrectly = quizDone || selected == quiz.correctIndex
    // Si ya estaba resuelta de antes, se muestra la opción correcta marcada.
    val shownSelection = if (quizDone) quiz.correctIndex else selected

    LearnFrame(
        photoPath = photoPath,
        photoStamp = photoStamp,
        onBack = onBack,
        onOpenProfile = onOpenProfile,
        onOpenSavings = onOpenSavings,
        onOpenGoals = onOpenGoals
    ) {
        LearnSectionTitle(title = stringResource(R.string.learn_pills_title))
        PillTeaserCard(onLearn = onLearn)

        QuizCard(
            quiz = quiz,
            selected = shownSelection,
            locked = answeredCorrectly,
            onSelect = { index ->
                if (!answeredCorrectly) {
                    selected = index
                    if (index == quiz.correctIndex) onCorrectAnswer()
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

@Composable
private fun PillTeaserCard(onLearn: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .learnCard()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.learn_pill_quote),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            ),
            color = FinInk
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.learn_pill_teaser),
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
                text = stringResource(R.string.learn_quiz_title),
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
