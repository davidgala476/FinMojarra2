package com.finmojarra.app.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finmojarra.app.R
import com.finmojarra.app.data.auth.AuthError
import com.finmojarra.app.data.auth.AuthRepository
import com.finmojarra.app.data.auth.AuthResult
import com.finmojarra.app.data.auth.SessionHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    @StringRes val identifierError: Int? = null,
    @StringRes val passwordError: Int? = null,
    val error: AuthError? = null,
    val loggedIn: Boolean = false
)

class LoginViewModel(
    private val repository: AuthRepository,
    private val sessionHolder: SessionHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onIdentifierChange(value: String) {
        _uiState.update { it.copy(identifier = value, identifierError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun submit() {
        val current = _uiState.value
        if (current.isLoading) return

        val identifierError = if (current.identifier.isBlank()) R.string.error_field_required else null
        val passwordError = if (current.password.isBlank()) R.string.error_field_required else null
        if (identifierError != null || passwordError != null) {
            _uiState.update {
                it.copy(identifierError = identifierError, passwordError = passwordError)
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = repository.login(current.identifier.trim(), current.password)) {
                is AuthResult.Success -> {
                    sessionHolder.set(result.data)
                    _uiState.update { it.copy(isLoading = false, loggedIn = true) }
                }
                is AuthResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }
    }

    /** Cierra el diálogo de error. Tras un fallo de credenciales se borra la contraseña. */
    fun dismissError() {
        _uiState.update { state ->
            val wasInvalid = state.error == AuthError.InvalidCredentials
            state.copy(error = null, password = if (wasInvalid) "" else state.password)
        }
    }
}
