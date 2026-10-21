package com.finmojarra.app.ui.goals

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finmojarra.app.R
import com.finmojarra.app.data.goals.CreateGoalResult
import com.finmojarra.app.data.goals.GoalsRepository
import java.util.Calendar
import java.util.TimeZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateGoalUiState(
    val name: String = "",
    /** Solo dígitos, sin puntos ni símbolos. */
    val amount: String = "",
    val deadlineMillis: Long? = null,
    @StringRes val nameError: Int? = null,
    @StringRes val amountError: Int? = null,
    @StringRes val deadlineError: Int? = null,
    val isSaving: Boolean = false,
    @StringRes val message: Int? = null,
    val created: Boolean = false
)

class CreateGoalViewModel(
    private val repository: GoalsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGoalUiState())
    val uiState: StateFlow<CreateGoalUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value.take(MAX_NAME), nameError = null) }
    }

    fun onAmountChange(value: String) {
        val digits = value.filter { it.isDigit() }.trimStart('0').take(MAX_DIGITS)
        _uiState.update { it.copy(amount = digits, amountError = null) }
    }

    /** [millis] llega del selector de fechas (medianoche UTC) o null para quitar la fecha. */
    fun onDeadlineChange(millis: Long?) {
        _uiState.update { it.copy(deadlineMillis = millis, deadlineError = null) }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun save() {
        val s = _uiState.value
        if (s.isSaving) return

        val nameError = if (s.name.isBlank()) R.string.error_field_required else null
        val amountError = if ((s.amount.toLongOrNull() ?: 0L) <= 0L) {
            if (s.amount.isBlank()) R.string.error_field_required else R.string.goals_error_amount
        } else null
        val deadlineError = s.deadlineMillis
            ?.takeIf { it < todayUtcMillis() }
            ?.let { R.string.goals_error_deadline_past }

        if (nameError != null || amountError != null || deadlineError != null) {
            _uiState.update {
                it.copy(nameError = nameError, amountError = amountError, deadlineError = deadlineError)
            }
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = repository.createGoal(
                name = s.name,
                targetAmount = s.amount.toLong().toDouble(),
                deadlineMillis = s.deadlineMillis
            )
            _uiState.update {
                when (result) {
                    CreateGoalResult.Success -> it.copy(isSaving = false, created = true)
                    is CreateGoalResult.Failure -> it.copy(
                        isSaving = false,
                        message = when (result.reason) {
                            CreateGoalResult.Reason.PermissionDenied -> R.string.goals_err_permission
                            CreateGoalResult.Reason.NoSession -> R.string.profile_err_no_session
                            CreateGoalResult.Reason.Unknown -> R.string.error_server_body
                        }
                    )
                }
            }
        }
    }

    /** Medianoche UTC del día de hoy según la fecha local (el selector trabaja en UTC). */
    private fun todayUtcMillis(): Long {
        val local = Calendar.getInstance()
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    private companion object {
        const val MAX_NAME = 40
        const val MAX_DIGITS = 12
    }
}
