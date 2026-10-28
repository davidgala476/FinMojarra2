package com.finmojarra.app.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finmojarra.app.R
import com.finmojarra.app.data.goals.Goal
import com.finmojarra.app.ui.components.FinPrimaryButton
import com.finmojarra.app.ui.theme.FinFieldBorder
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinMutedText
import com.finmojarra.app.ui.theme.FinTeal
import com.finmojarra.app.ui.theme.FinWhite

/** Metas: pestañas, tarjetas con progreso y foto de referencia, y el botón "+" para crear. */
@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel,
    photoPath: String,
    photoStamp: Long,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenGoal: (String) -> Unit,
    onCreateGoal: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    GoalsScaffold(
        onBack = onBack,
        onOpenProfile = onOpenProfile,
        photoPath = photoPath,
        photoStamp = photoStamp,
        floatingAction = {
            if (!state.isLoading && !state.hasError) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .safeDrawingPadding()
                        .padding(end = 20.dp, bottom = 20.dp)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(FinTeal)
                        .clickable(role = Role.Button, onClick = onCreateGoal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.goals_create_button),
                        tint = FinWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        GoalsTabs(
            selected = GoalsTab.Goals,
            onSavings = onOpenSavings,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> CenteredBox {
                    CircularProgressIndicator(color = FinTeal)
                }

                state.hasError -> CenteredBox {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    ) {
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
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 96.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(state.goals, key = { _, goal -> goal.id }) { index, goal ->
                        GoalCard(
                            goal = goal,
                            number = index + 1,
                            onClick = { onOpenGoal(goal.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun GoalCard(goal: Goal, number: Int, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(FinWhite)
            .border(1.dp, FinFieldBorder, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoalBadgeIcon()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.goals_card_title, number, goal.name),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = FinInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.goals_target_line, formatMoney(goal.targetAmount)),
                    style = MaterialTheme.typography.bodySmall,
                    color = FinMutedText
                )
            }
            GoalChip()
        }

        Spacer(modifier = Modifier.height(12.dp))
        GoalProgress(saved = goal.savedAmount, progress = goal.progress)
        Spacer(modifier = Modifier.height(10.dp))
        GoalStats(remaining = goal.remainingAmount, target = goal.targetAmount)

        goal.imageBase64?.let { image ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.goals_reference_photo),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = FinInk
            )
            Spacer(modifier = Modifier.height(6.dp))
            GoalImage(
                base64 = image,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
        }
    }
}
