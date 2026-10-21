package com.finmojarra.app.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finmojarra.app.R
import com.finmojarra.app.data.auth.AuthError
import com.finmojarra.app.data.auth.AuthRepository
import com.finmojarra.app.data.auth.AuthResult
import com.finmojarra.app.data.auth.RegisterData
import com.finmojarra.app.data.auth.SessionHolder
import com.finmojarra.app.data.profile.ProfileDetailsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    @StringRes val fullNameError: Int? = null,
    @StringRes val emailError: Int? = null,
    @StringRes val phoneError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val confirmPasswordError: Int? = null,
    val error: AuthError? = null,
    val registered: Boolean = false
)

class RegisterViewModel(
    private val repository: AuthRepository,
    private val sessionHolder: SessionHolder,
    initialEmail: String = "",
    private val detailsRepository: ProfileDetailsRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState(email = initialEmail))
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, fullNameError = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, phoneError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
    }

    fun submit() {
        val s = _uiState.value
        if (s.isLoading) return

        val fullNameError = if (s.fullName.isBlank()) R.string.error_field_required else null
        val emailError = when {
            s.email.isBlank() -> R.string.error_field_required
            !Validators.isValidEmail(s.email) -> R.string.error_email_invalid
            else -> null
        }
        val phoneError = when {
            s.phone.isBlank() -> R.string.error_field_required
            !Validators.isValidPhone(s.phone) -> R.string.error_phone_invalid
            else -> null
        }
        val passwordError = when {
            s.password.isBlank() -> R.string.error_field_required
            s.password.length < Validators.MIN_PASSWORD_LENGTH -> R.string.error_password_short
            else -> null
        }
        val confirmError = when {
            s.confirmPassword.isBlank() -> R.string.error_field_required
            s.confirmPassword != s.password -> R.string.error_password_mismatch
            else -> null
        }

        if (listOf(fullNameError, emailError, phoneError, passwordError, confirmError).any { it != null }) {
            _uiState.update {
                it.copy(
                    fullNameError = fullNameError,
                    emailError = emailError,
                    phoneError = phoneError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val data = RegisterData(
                fullName = s.fullName.trim(),
                email = s.email.trim(),
                phone = s.phone.trim(),
                password = s.password
            )
            when (val result = repository.register(data)) {
                is AuthResult.Success -> {
                    sessionHolder.set(result.data)
                    // El teléfono del registro queda guardado en el perfil.
                    detailsRepository?.save { it.copy(phone = data.phone) }
                    _uiState.update { it.copy(isLoading = false, registered = true) }
                }
                is AuthResult.Failure -> {
                    if (result.error == AuthError.UserAlreadyExists) {
                        _uiState.update {
                            it.copy(isLoading = false, emailError = R.string.error_email_taken)
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = result.error) }
                    }
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
