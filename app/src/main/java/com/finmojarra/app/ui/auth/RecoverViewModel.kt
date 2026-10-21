package com.finmojarra.app.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finmojarra.app.R
import com.finmojarra.app.data.auth.AuthError
import com.finmojarra.app.data.auth.AuthRepository
import com.finmojarra.app.data.auth.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecoverUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    @StringRes val emailError: Int? = null,
    val error: AuthError? = null,
    /** Mensaje de confirmación que se muestra en un diálogo informativo. */
    @StringRes val infoMessage: Int? = null
)

class RecoverViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecoverUiState())
    val uiState: StateFlow<RecoverUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun recoverPassword() {
        val s = _uiState.value
        if (s.isLoading) return

        val emailError = when {
            s.email.isBlank() -> R.string.error_field_required
            !Validators.isValidEmail(s.email) -> R.string.error_email_invalid
            else -> null
        }
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = repository.requestPasswordReset(s.email.trim())) {
                is AuthResult.Success -> _uiState.update {
                    it.copy(isLoading = false, infoMessage = R.string.recover_password_sent)
                }
                is AuthResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, error = result.error)
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun dismissInfo() {
        _uiState.update { it.copy(infoMessage = null) }
    }
}
