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

/** Texto de un diálogo emergente: recurso y, si lo necesita, sus argumentos (montos). */
data class GoalMessage(@StringRes val res: Int, val args: List<Any> = emptyList())

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
    /** Muestra la confirmación "¿Eliminar esta meta?". */
    val confirmDelete: Boolean = false,
    /** La meta ya se borró: la pantalla debe cerrarse. */
    val deleted: Boolean = false,
    val message: GoalMessage? = null
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

    fun askDelete() {
        _uiState.update { it.copy(confirmDelete = true, dialogOpen = false) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(confirmDelete = false) }
    }

    fun confirmDelete() {
        val s = _uiState.value
        if (s.isWorking || s.goal == null) return
        // isWorking tapa la pantalla con la carga mientras se borra, para que no se vea
        // "meta no encontrada" cuando Firestore quita la meta de la lista local.
        _uiState.update { it.copy(confirmDelete = false, isWorking = true) }
        viewModelScope.launch {
            val result = repository.deleteGoal(goalId)
            _uiState.update {
                when (result) {
                    GoalWriteResult.Success -> it.copy(isWorking = false, deleted = true)
                    is GoalWriteResult.Failure ->
                        it.copy(isWorking = false, message = GoalMessage(result.reason.messageRes()))
                }
            }
        }
    }

    fun deposit() = move(+1)

    fun withdraw() = move(-1)

    private fun move(sign: Int) {
        val s = _uiState.value
        val goal = s.goal ?: return
        if (s.isWorking) return

        // Casos que se avisan con una ventana emergente (el diálogo de valor queda abierto).
        if (sign > 0 && goal.isCompleted) {
            showMessage(GoalMessage(R.string.goals_msg_completed))
            return
        }
        if (sign < 0 && goal.savedAmount <= 0.0) {
            showMessage(GoalMessage(R.string.goals_msg_nothing_saved))
            return
        }

        val value = s.amount.toLongOrNull() ?: 0L
        val inlineError = when {
            s.amount.isBlank() -> R.string.error_field_required
            value <= 0L -> R.string.goals_error_amount
            else -> null
        }
        if (inlineError != null) {
            _uiState.update { it.copy(amountError = inlineError) }
            return
        }

        if (sign > 0 && value > goal.remainingAmount) {
            showMessage(
                GoalMessage(R.string.goals_msg_deposit_exceeds, listOf(formatMoney(goal.remainingAmount)))
            )
            return
        }
        if (sign < 0 && value > goal.savedAmount) {
            showMessage(
                GoalMessage(
                    R.string.goals_msg_withdraw_exceeds,
                    listOf(formatMoney(value.toDouble()), formatMoney(goal.savedAmount))
                )
            )
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
                        it.copy(isWorking = false, message = GoalMessage(result.reason.messageRes()))
                }
            }
        }
    }

    private fun showMessage(message: GoalMessage) {
        _uiState.update { it.copy(message = message) }
    }

    private companion object {
        const val MAX_DIGITS = 12
    }
}
