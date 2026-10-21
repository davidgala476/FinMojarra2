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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finmojarra.app.R
import com.finmojarra.app.data.goals.Goal
import com.finmojarra.app.ui.components.DialogActionButton
import com.finmojarra.app.ui.components.FinDialog
import com.finmojarra.app.ui.components.FinPrimaryButton
import com.finmojarra.app.ui.components.FinTextField
import com.finmojarra.app.ui.theme.FinError
import com.finmojarra.app.ui.theme.FinFieldBorder
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinMutedText
import com.finmojarra.app.ui.theme.FinTeal
import com.finmojarra.app.ui.theme.FinWhite

/** Detalle de una meta ("Meta 1"): imagen, progreso y botones Ingresar / Retirar. */
@Composable
fun GoalDetailScreen(
    viewModel: GoalDetailViewModel,
    photoPath: String,
    photoStamp: Long,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onLearnToSave: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val goal = state.goal

    GoalsScaffold(
        onBack = onBack,
        onOpenProfile = onOpenProfile,
        photoPath = photoPath,
        photoStamp = photoStamp,
        isLoading = state.isWorking
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        GoalsTabs(modifier = Modifier.padding(horizontal = 24.dp))

        when {
            state.isLoading -> Box(Modifier.weight(1f).fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = FinTeal)
            }

            goal == null -> Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(
                        if (state.hasError) R.string.goals_error else R.string.goals_not_found
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = FinInk,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                FinPrimaryButton(text = stringResource(R.string.action_back), onClick = onBack)
            }

            else -> GoalDetailContent(
                goal = goal,
                number = state.number,
                onMoveMoney = viewModel::openDialog,
                onLearnToSave = onLearnToSave,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (state.dialogOpen && goal != null) {
        FinDialog(title = stringResource(R.string.goals_move_title), onDismiss = viewModel::closeDialog) {
            GoalProgress(saved = goal.savedAmount, progress = goal.progress)
            FinTextField(
                value = state.amount,
                onValueChange = viewModel::onAmountChange,
                label = stringResource(R.string.goals_move_value),
                placeholder = stringResource(R.string.goals_value_placeholder),
                errorText = state.amountError?.let { stringResource(it) },
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { viewModel.deposit() })
            )
            state.amount.toLongOrNull()?.let {
                Text(
                    text = formatMoney(it.toDouble()),
                    style = MaterialTheme.typography.bodySmall,
                    color = FinMutedText
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DialogActionButton(
                    text = stringResource(R.string.goals_action_deposit),
                    onClick = viewModel::deposit,
                    modifier = Modifier.fillMaxWidth()
                )
                DialogActionButton(
                    text = stringResource(R.string.goals_action_withdraw),
                    onClick = viewModel::withdraw,
                    contentColor = FinError,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    state.message?.let { messageRes ->
        FinDialog(title = stringResource(messageRes), onDismiss = viewModel::dismissMessage) {
            DialogActionButton(
                text = stringResource(R.string.action_accept),
                onClick = viewModel::dismissMessage,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GoalDetailContent(
    goal: Goal,
    number: Int,
    onMoveMoney: () -> Unit,
    onLearnToSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.goals_detail_title, number),
            style = MaterialTheme.typography.titleMedium,
            color = FinInk
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(FinWhite)
                .border(1.dp, FinFieldBorder, shape)
                .padding(14.dp)
        ) {
            goal.imageBase64?.let { image ->
                GoalImage(
                    base64 = image,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoalBadgeIcon()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                ) {
                    Text(
                        text = goal.name,
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
            }

            Spacer(modifier = Modifier.height(14.dp))
            GoalProgress(saved = goal.savedAmount, progress = goal.progress)
            Spacer(modifier = Modifier.height(10.dp))
            GoalStats(remaining = goal.remainingAmount, target = goal.targetAmount)

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SmallTealButton(stringResource(R.string.goals_deposit_button), onMoveMoney)
                SmallTealButton(stringResource(R.string.goals_withdraw_button), onMoveMoney)
            }

            if (goal.isCompleted) {
                Text(
                    text = stringResource(R.string.goals_completed),
                    style = MaterialTheme.typography.bodySmall,
                    color = FinTeal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onLearnToSave)
                .padding(8.dp)
        ) {
            Text(
                text = stringResource(R.string.goals_help_line1),
                style = MaterialTheme.typography.bodySmall,
                color = FinMutedText,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.goals_help_line2),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = FinInk,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SmallTealButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = FinTeal, contentColor = FinWhite),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
    }
}
