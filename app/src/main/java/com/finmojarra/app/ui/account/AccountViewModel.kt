package com.finmojarra.app.ui.account

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finmojarra.app.R
import com.finmojarra.app.data.account.AccountRepository
import com.finmojarra.app.data.account.AccountResult
import com.finmojarra.app.data.account.AccountWriteResult
import com.finmojarra.app.data.account.Movement
import com.finmojarra.app.data.account.MovementType
import com.finmojarra.app.ui.goals.GoalMessage
import com.finmojarra.app.ui.goals.formatMoney
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val balance: Double = 0.0,
    /** Del más reciente al más antiguo. */
    val movements: List<Movement> = emptyList(),
    /** Qué diálogo está abierto: ingresar, retirar o ninguno (null). */
    val dialog: MovementType? = null,
    /** Solo dígitos. */
    val amount: String = "",
    @StringRes val amountError: Int? = null,
    val isWorking: Boolean = false,
    val message: GoalMessage? = null
) {
    /** Ya se puede ingresar o retirar: el saldo cargó bien. */
    val canMove: Boolean get() = !isLoading && !hasError
}

/** Saldo de la cuenta del usuario y sus movimientos, con ingreso y retiro de dinero. */
class AccountViewModel(
    private val repository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observe()
    }

    private fun observe() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repository.observeAccount().collect { result ->
                _uiState.update { state ->
                    when (result) {
                        is AccountResult.Success -> state.copy(
                            isLoading = false,
                            hasError = false,
                            balance = result.balance,
                            movements = result.movements
                        )
                        AccountResult.Failure -> state.copy(isLoading = false, hasError = true)
                    }
                }
            }
        }
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, hasError = false) }
        observe()
    }

    fun openDialog(type: MovementType) {
        if (!_uiState.value.canMove) return
        _uiState.update { it.copy(dialog = type, amount = "", amountError = null) }
    }

    fun closeDialog() {
        _uiState.update { it.copy(dialog = null, amount = "", amountError = null) }
    }

    fun onAmountChange(value: String) {
        val digits = value.filter { it.isDigit() }.trimStart('0').take(MAX_DIGITS)
        _uiState.update { it.copy(amount = digits, amountError = null) }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(message = null) }
    }

    /** Confirma el diálogo abierto: ingresa o retira el valor escrito. */
    fun confirm() {
        val s = _uiState.value
        val type = s.dialog ?: return
        if (s.isWorking || !s.canMove) return

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

        if (type == MovementType.Withdraw) {
            if (s.balance <= 0.0) {
                showMessage(GoalMessage(R.string.account_msg_nothing))
                return
            }
            if (value > s.balance) {
                showMessage(
                    GoalMessage(
                        R.string.account_msg_exceeds,
                        listOf(formatMoney(value.toDouble()), formatMoney(s.balance))
                    )
                )
                return
            }
        }

        _uiState.update { it.copy(isWorking = true) }
        viewModelScope.launch {
            val result = repository.move(type, value.toDouble())
            _uiState.update {
                when (result) {
                    AccountWriteResult.Success ->
                        it.copy(isWorking = false, dialog = null, amount = "")
                    is AccountWriteResult.Failure ->
                        it.copy(isWorking = false, message = GoalMessage(result.reason.messageRes()))
                }
            }
        }
    }

    private fun showMessage(message: GoalMessage) {
        _uiState.update { it.copy(message = message) }
    }

    private fun AccountWriteResult.Reason.messageRes(): Int = when (this) {
        AccountWriteResult.Reason.PermissionDenied -> R.string.account_err_permission
        AccountWriteResult.Reason.NoSession -> R.string.profile_err_no_session
        AccountWriteResult.Reason.Unknown -> R.string.error_server_body
    }

    private companion object {
        const val MAX_DIGITS = 12
    }
}
