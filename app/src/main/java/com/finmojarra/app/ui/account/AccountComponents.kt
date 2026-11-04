package com.finmojarra.app.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.finmojarra.app.R
import com.finmojarra.app.data.account.Movement
import com.finmojarra.app.data.account.MovementType
import com.finmojarra.app.ui.components.DialogActionButton
import com.finmojarra.app.ui.components.FinDialog
import com.finmojarra.app.ui.components.FinLinkText
import com.finmojarra.app.ui.components.FinTextField
import com.finmojarra.app.ui.goals.formatMoney
import com.finmojarra.app.ui.home.HomeCard
import com.finmojarra.app.ui.theme.FinError
import com.finmojarra.app.ui.theme.FinFieldBorder
import com.finmojarra.app.ui.theme.FinInk
import com.finmojarra.app.ui.theme.FinMutedText
import com.finmojarra.app.ui.theme.FinTeal
import com.finmojarra.app.ui.theme.FinWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MAX_SHOWN = 10

/** "Tu dinero": saldo de la cuenta y los botones Ingresar / Retirar. */
@Composable
fun AccountCard(
    state: AccountUiState,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    onRetry: () -> Unit
) {
    HomeCard {
        Text(
            text = stringResource(R.string.account_title),
            style = MaterialTheme.typography.bodySmall,
            color = FinMutedText
        )
        Spacer(modifier = Modifier.height(4.dp))

        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                CircularProgressIndicator(
                    color = FinTeal,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            }

            state.hasError -> Column {
                Text(
                    text = stringResource(R.string.goals_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = FinMutedText
                )
                FinLinkText(text = stringResource(R.string.action_retry), onClick = onRetry)
            }

            else -> {
                Text(
                    text = formatMoney(state.balance),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FinInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.account_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = FinMutedText
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onDeposit,
                enabled = state.canMove,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FinTeal,
                    contentColor = FinWhite,
                    disabledContainerColor = FinTeal.copy(alpha = 0.4f),
                    disabledContentColor = FinWhite
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Text(
                    text = stringResource(R.string.account_deposit),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )
            }
            OutlinedButton(
                onClick = onWithdraw,
                enabled = state.canMove,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (state.canMove) FinTeal else FinFieldBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FinTeal),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Text(
                    text = stringResource(R.string.account_withdraw),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

/** "Movimientos recientes": los últimos ingresos y retiros. */
@Composable
fun MovementsCard(state: AccountUiState) {
    HomeCard {
        Text(
            text = stringResource(R.string.movements_title),
            style = MaterialTheme.typography.titleMedium,
            color = FinInk
        )
        Spacer(modifier = Modifier.height(8.dp))

        when {
            state.isLoading || state.hasError -> Unit

            state.movements.isEmpty() -> Text(
                text = stringResource(R.string.movements_empty),
                style = MaterialTheme.typography.bodySmall,
                color = FinMutedText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            else -> {
                val shown = state.movements.take(MAX_SHOWN)
                val dateFormat = remember { SimpleDateFormat("d MMM yyyy, h:mm a", Locale("es", "CO")) }
                shown.forEachIndexed { index, movement ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(FinFieldBorder)
                        )
                    }
                    MovementRow(movement = movement, dateFormat = dateFormat)
                }
                if (state.movements.size > MAX_SHOWN) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.movements_more, MAX_SHOWN),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = FinMutedText
                    )
                }
            }
        }
    }
}

@Composable
private fun MovementRow(movement: Movement, dateFormat: SimpleDateFormat) {
    val isDeposit = movement.type == MovementType.Deposit
    val color = if (isDeposit) FinTeal else FinError
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isDeposit) "+" else "−",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
        ) {
            Text(
                text = stringResource(if (isDeposit) R.string.movement_deposit else R.string.movement_withdraw),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = FinInk
            )
            Text(
                text = movement.createdAtMillis?.let { dateFormat.format(Date(it)) }
                    ?: stringResource(R.string.movement_just_now),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = FinMutedText
            )
        }
        Text(
            text = (if (isDeposit) "+" else "−") + formatMoney(movement.amount),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = color,
            maxLines = 1
        )
    }
}

/** Diálogo para escribir el valor a ingresar o retirar. */
@Composable
fun MoveMoneyDialog(
    state: AccountUiState,
    onAmountChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val type = state.dialog ?: return
    val isDeposit = type == MovementType.Deposit
    FinDialog(
        title = stringResource(
            if (isDeposit) R.string.account_deposit_title else R.string.account_withdraw_title
        ),
        onDismiss = onDismiss
    ) {
        if (!isDeposit) {
            Text(
                text = stringResource(R.string.account_available, formatMoney(state.balance)),
                style = MaterialTheme.typography.bodySmall,
                color = FinMutedText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        FinTextField(
            value = state.amount,
            onValueChange = onAmountChange,
            label = stringResource(R.string.goals_move_value),
            placeholder = stringResource(R.string.goals_value_placeholder),
            errorText = state.amountError?.let { stringResource(it) },
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { onConfirm() })
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
                text = stringResource(
                    if (isDeposit) R.string.account_deposit else R.string.account_withdraw
                ),
                onClick = onConfirm,
                contentColor = if (isDeposit) FinTeal else FinError,
                modifier = Modifier.fillMaxWidth()
            )
            DialogActionButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Aviso emergente (saldo insuficiente, error al guardar...). */
@Composable
fun AccountMessageDialog(state: AccountUiState, onDismiss: () -> Unit) {
    val message = state.message ?: return
    FinDialog(
        title = stringResource(message.res, *message.args.toTypedArray()),
        onDismiss = onDismiss
    ) {
        DialogActionButton(
            text = stringResource(R.string.action_accept),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
