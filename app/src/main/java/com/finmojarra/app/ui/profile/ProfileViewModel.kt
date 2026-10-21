package com.finmojarra.app.ui.profile

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finmojarra.app.R
import com.finmojarra.app.data.auth.SessionHolder
import com.finmojarra.app.data.profile.ProfileDetails
import com.finmojarra.app.data.profile.ProfileDetailsRepository
import com.finmojarra.app.data.profile.ProfileError
import com.finmojarra.app.data.profile.ProfileRepository
import com.finmojarra.app.data.profile.ProfileResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProfileDialog { None, ChangePassword, DeleteAccount, ConfirmLogout }

data class ProfileUiState(
    val name: String = "",
    val savedName: String = "",
    val email: String = "",
    val username: String = "",
    val hasPassword: Boolean = true,
    @StringRes val nameError: Int? = null,
    val isLoading: Boolean = false,
    val dialog: ProfileDialog = ProfileDialog.None,
    // Campos de los diálogos de contraseña / eliminar cuenta.
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    @StringRes val currentPasswordError: Int? = null,
    @StringRes val newPasswordError: Int? = null,
    @StringRes val confirmPasswordError: Int? = null,
    /** Mensaje informativo o de error que se muestra en un diálogo con "Aceptar". */
    @StringRes val message: Int? = null,
    val loggedOut: Boolean = false,
    /** Teléfono, dirección, documento, info demográfica y foto. */
    val details: ProfileDetails = ProfileDetails()
) {
    val canSaveName: Boolean get() = name.trim() != savedName && name.isNotBlank()
}

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val sessionHolder: SessionHolder,
    private val detailsRepository: ProfileDetailsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        detailsRepository.reload()
        viewModelScope.launch {
            detailsRepository.details.collect { details ->
                _uiState.update { it.copy(details = details) }
            }
        }
    }

    private fun initialState(): ProfileUiState {
        val profile = repository.currentProfile()
        val session = sessionHolder.session.value
        return ProfileUiState(
            name = profile?.name ?: session?.displayName.orEmpty(),
            savedName = profile?.name ?: session?.displayName.orEmpty(),
            email = profile?.email ?: session?.email.orEmpty(),
            username = profile?.username ?: session?.username.orEmpty(),
            hasPassword = profile?.hasPassword ?: true,
            details = detailsRepository.details.value
        )
    }

    // ---------- Nombre ----------

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value.take(MAX_NAME), nameError = null) }
    }

    fun saveName() {
        val current = _uiState.value
        if (current.isLoading) return
        if (current.name.isBlank()) {
            _uiState.update { it.copy(nameError = R.string.error_field_required) }
            return
        }
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = repository.updateDisplayName(current.name)) {
                is ProfileResult.Success -> {
                    sessionHolder.set(
                        sessionHolder.session.value?.copy(displayName = result.data.name)
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = result.data.name,
                            savedName = result.data.name,
                            message = R.string.profile_msg_name_saved
                        )
                    }
                }
                is ProfileResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, message = result.error.toMessage()) }
            }
        }
    }

    // ---------- Contraseña ----------

    fun openChangePassword() = openDialog(ProfileDialog.ChangePassword)

    fun onCurrentPasswordChange(value: String) {
        _uiState.update { it.copy(currentPassword = value, currentPasswordError = null) }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, newPasswordError = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
    }

    fun submitChangePassword() {
        val s = _uiState.value
        if (s.isLoading) return
        val currentErr = if (s.currentPassword.isBlank()) R.string.error_field_required else null
        val newErr = when {
            s.newPassword.isBlank() -> R.string.error_field_required
            s.newPassword.length < MIN_PASSWORD -> R.string.error_password_short
            else -> null
        }
        val confirmErr = if (s.confirmPassword != s.newPassword) R.string.error_password_mismatch else null
        if (currentErr != null || newErr != null || confirmErr != null) {
            _uiState.update {
                it.copy(
                    currentPasswordError = currentErr,
                    newPasswordError = newErr,
                    confirmPasswordError = confirmErr
                )
            }
            return
        }
        _uiState.update { it.copy(isLoading = true, dialog = ProfileDialog.None) }
        viewModelScope.launch {
            when (val result = repository.changePassword(s.currentPassword, s.newPassword)) {
                is ProfileResult.Success ->
                    _uiState.update {
                        it.clearPasswordFields().copy(
                            isLoading = false,
                            message = R.string.profile_msg_password_changed
                        )
                    }
                is ProfileResult.Failure ->
                    _uiState.update {
                        it.clearPasswordFields()
                            .copy(isLoading = false, message = result.error.toMessage())
                    }
            }
        }
    }

    fun sendResetEmail() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = repository.sendPasswordResetEmail()) {
                is ProfileResult.Success ->
                    _uiState.update {
                        it.copy(isLoading = false, message = R.string.profile_msg_reset_sent)
                    }
                is ProfileResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, message = result.error.toMessage()) }
            }
        }
    }

    // ---------- Cerrar sesión / eliminar cuenta ----------

    fun openLogout() = openDialog(ProfileDialog.ConfirmLogout)

    fun confirmLogout() {
        repository.signOut()
        sessionHolder.set(null)
        _uiState.update { it.copy(dialog = ProfileDialog.None, loggedOut = true) }
    }

    fun openDeleteAccount() = openDialog(ProfileDialog.DeleteAccount)

    fun confirmDeleteAccount() {
        val s = _uiState.value
        if (s.isLoading) return
        if (s.hasPassword && s.currentPassword.isBlank()) {
            _uiState.update { it.copy(currentPasswordError = R.string.error_field_required) }
            return
        }
        _uiState.update { it.copy(isLoading = true, dialog = ProfileDialog.None) }
        viewModelScope.launch {
            when (val result = repository.deleteAccount(s.currentPassword.takeIf { s.hasPassword })) {
                is ProfileResult.Success -> {
                    repository.signOut()
                    sessionHolder.set(null)
                    _uiState.update { it.copy(isLoading = false, loggedOut = true) }
                }
                is ProfileResult.Failure ->
                    _uiState.update {
                        it.clearPasswordFields()
                            .copy(isLoading = false, message = result.error.toMessage())
                    }
            }
        }
    }

    // ---------- Diálogos ----------

    fun dismissDialog() {
        _uiState.update { it.clearPasswordFields().copy(dialog = ProfileDialog.None) }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun openDialog(dialog: ProfileDialog) {
        _uiState.update { it.clearPasswordFields().copy(dialog = dialog) }
    }

    private fun ProfileUiState.clearPasswordFields() = copy(
        currentPassword = "",
        newPassword = "",
        confirmPassword = "",
        currentPasswordError = null,
        newPasswordError = null,
        confirmPasswordError = null
    )

    @StringRes
    private fun ProfileError.toMessage(): Int = when (this) {
        ProfileError.WrongPassword -> R.string.profile_err_wrong_password
        ProfileError.WeakPassword -> R.string.profile_err_weak_password
        ProfileError.RequiresRecentLogin -> R.string.profile_err_recent_login
        ProfileError.NoConnection -> R.string.error_no_connection_generic
        ProfileError.TooManyAttempts -> R.string.error_too_many_attempts
        ProfileError.NoSession -> R.string.profile_err_no_session
        is ProfileError.Unknown -> R.string.error_server_body
    }

    private companion object {
        const val MIN_PASSWORD = 8
        const val MAX_NAME = 60
    }
}
