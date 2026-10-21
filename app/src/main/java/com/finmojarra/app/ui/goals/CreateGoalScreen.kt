package com.finmojarra.app.ui.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finmojarra.app.R
import com.finmojarra.app.ui.components.AuthScreenFrame
import com.finmojarra.app.ui.components.DialogActionButton
import com.finmojarra.app.ui.components.FinDialog
import com.finmojarra.app.ui.components.FinLinkText
import com.finmojarra.app.ui.components.FinPrimaryButton
import com.finmojarra.app.ui.components.FinTextField
import com.finmojarra.app.ui.theme.FinInk
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val colombia = Locale("es", "CO")

/** Parte 2 de Metas: formulario para crear una meta (nombre, monto y fecha límite opcional). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGoalScreen(
    viewModel: CreateGoalViewModel,
    onBack: () -> Unit,
    onCreated: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.created) {
        if (state.created) onCreated()
    }

    AuthScreenFrame(onBack = onBack, isLoading = state.isSaving) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = stringResource(R.string.goals_create_title),
            style = MaterialTheme.typography.titleLarge,
            color = FinInk,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FinTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = stringResource(R.string.goals_name_label),
                placeholder = stringResource(R.string.goals_name_placeholder),
                errorText = state.nameError?.let { stringResource(it) }
            )

            FinTextField(
                value = state.amount,
                onValueChange = viewModel::onAmountChange,
                label = stringResource(R.string.goals_amount_label),
                placeholder = stringResource(R.string.goals_amount_placeholder),
                errorText = state.amountError?.let { stringResource(it) },
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { viewModel.save() })
            )
            state.amount.toLongOrNull()?.let { value ->
                Text(
                    text = NumberFormat.getCurrencyInstance(colombia)
                        .apply { maximumFractionDigits = 0 }
                        .format(value),
                    style = MaterialTheme.typography.bodySmall,
                    color = FinInk
                )
            }

            // Campo de fecha: es solo de lectura y al tocarlo abre el selector.
            Box {
                FinTextField(
                    value = state.deadlineMillis?.let { formatDate(it) }.orEmpty(),
                    onValueChange = {},
                    label = stringResource(R.string.goals_deadline_label),
                    placeholder = stringResource(R.string.goals_deadline_placeholder),
                    errorText = state.deadlineError?.let { stringResource(it) },
                    enabled = false
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showPicker = true }
                )
            }
            if (state.deadlineMillis != null) {
                FinLinkText(
                    text = stringResource(R.string.goals_remove_deadline),
                    onClick = { viewModel.onDeadlineChange(null) }
                )
            }

            FinPrimaryButton(
                text = stringResource(R.string.goals_create_button),
                onClick = viewModel::save
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.deadlineMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(viewModel::onDeadlineChange)
                    showPicker = false
                }) { Text(stringResource(R.string.action_accept)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = pickerState)
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

private fun formatDate(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", colombia).apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(millis))
