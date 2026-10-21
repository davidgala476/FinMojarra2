package com.finmojarra.app.ui.goals

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finmojarra.app.R
import com.finmojarra.app.data.goals.Goal
import com.finmojarra.app.data.goals.GoalWriteResult
import com.finmojarra.app.data.goals.GoalsRepository
import com.finmojarra.app.data.goals.GoalsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GoalDetailUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val goal: Goal? = null,
    /** Posición de la meta en la lista ("Meta 2"). */
    val number: Int = 0,
    val dialogOpen: Boolean = false,
    /** Solo dígitos. */
    val amount: String = "",
    @StringRes val amountError: Int? = null,
    val isWorking: Boolean = false,
    @StringRes val message: Int? = null
)

/** Detalle de una meta e ingreso / retiro de dinero. */
class GoalDetailViewModel(
    private val repository: GoalsRepository,
    private val goalId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalDetailUiState())
    val uiState: StateFlow<GoalDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeGoals().collect { result ->
                _uiState.update { state ->
                    when (result) {
                        is GoalsResult.Success -> {
                            val index = result.goals.indexOfFirst { it.id == goalId }
                            state.copy(
                                isLoading = false,
                                hasError = false,
                                goal = result.goals.getOrNull(index),
                                number = index + 1
                            )
                        }
                        GoalsResult.Failure -> state.copy(isLoading = false, hasError = true)
                    }
                }
            }
        }
    }

    fun openDialog() {
        _uiState.update { it.copy(dialogOpen = true, amount = "", amountError = null) }
    }

    fun closeDialog() {
        _uiState.update { it.copy(dialogOpen = false, amount = "", amountError = null) }
    }

    fun onAmountChange(value: String) {
        val digits = value.filter { it.isDigit() }.trimStart('0').take(MAX_DIGITS)
        _uiState.update { it.copy(amount = digits, amountError = null) }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun deposit() = move(+1)

    fun withdraw() = move(-1)

    private fun move(sign: Int) {
        val s = _uiState.value
        val goal = s.goal ?: return
        if (s.isWorking) return

        val value = s.amount.toLongOrNull() ?: 0L
        val error = when {
            s.amount.isBlank() -> R.string.error_field_required
            value <= 0L -> R.string.goals_error_amount
            sign < 0 && value > goal.savedAmount -> R.string.goals_error_withdraw
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(amountError = error) }
            return
        }

        _uiState.update { it.copy(isWorking = true) }
        viewModelScope.launch {
            val result = repository.addToSaved(goalId, sign * value.toDouble())
            _uiState.update {
                when (result) {
                    GoalWriteResult.Success ->
                        it.copy(isWorking = false, dialogOpen = false, amount = "")
                    is GoalWriteResult.Failure ->
                        it.copy(isWorking = false, message = result.reason.messageRes())
                }
            }
        }
    }

    private companion object {
        const val MAX_DIGITS = 12
    }
}
