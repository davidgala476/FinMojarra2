package com.finmojarra.app.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finmojarra.app.R
import com.finmojarra.app.data.goals.Goal
import com.finmojarra.app.ui.components.CloudDecoration
import com.finmojarra.app.ui.components.FinPrimaryButton
import com.finmojarra.app.ui.theme.FinBackground
import com.finmojarra.app.ui.theme.FinFieldBorder
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinMutedText
import com.finmojarra.app.ui.theme.FinSlate900
import com.finmojarra.app.ui.theme.FinTeal
import com.finmojarra.app.ui.theme.FinWhite
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val colombia = Locale("es", "CO")

private fun formatMoney(amount: Double): String =
    NumberFormat.getCurrencyInstance(colombia).apply { maximumFractionDigits = 0 }.format(amount)

private fun formatDate(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", colombia).apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(millis))

/** Metas: lista con progreso y botón para crear una nueva (editar y mover dinero: parte 3). */
@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel,
    onBack: () -> Unit,
    onCreateGoal: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FinBackground)
    ) {
        CloudDecoration(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(72.dp))
            Text(
                text = stringResource(R.string.goals_title),
                style = MaterialTheme.typography.titleLarge,
                color = FinInk,
                modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> CenteredBox {
                    CircularProgressIndicator(color = FinTeal)
                }

                state.hasError -> CenteredBox {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.goals_error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = FinInk,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        FinPrimaryButton(
                            text = stringResource(R.string.action_retry),
                            onClick = viewModel::retry
                        )
                    }
                }

                state.goals.isEmpty() -> CenteredBox {
                    Text(
                        text = stringResource(R.string.goals_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = FinMutedText,
                        textAlign = TextAlign.Center
                    )
                }

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.goals, key = { it.id }) { goal -> GoalCard(goal) }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
            }

            if (!state.isLoading && !state.hasError) {
                FinPrimaryButton(
                    text = stringResource(R.string.goals_create_button),
                    onClick = onCreateGoal,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .safeDrawingPadding()
                .padding(start = 20.dp, top = 12.dp)
                .size(48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_left),
                contentDescription = stringResource(R.string.action_back),
                tint = FinSlate900,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun GoalCard(goal: Goal) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(FinWhite, shape)
            .border(1.dp, FinFieldBorder, shape)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = goal.name,
                style = MaterialTheme.typography.titleMedium,
                color = FinInk,
                maxLines = 2,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            Text(
                text = "${(goal.progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = FinTeal
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { goal.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = FinTeal,
            trackColor = FinFieldBorder,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(
                R.string.goals_saved_of,
                formatMoney(goal.savedAmount),
                formatMoney(goal.targetAmount)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = FinInk
        )

        if (goal.isCompleted) {
            Text(
                text = stringResource(R.string.goals_completed),
                style = MaterialTheme.typography.bodySmall,
                color = FinTeal,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            goal.deadlineMillis?.let {
                Text(
                    text = stringResource(R.string.goals_deadline, formatDate(it)),
                    style = MaterialTheme.typography.bodySmall,
                    color = FinMutedText,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
