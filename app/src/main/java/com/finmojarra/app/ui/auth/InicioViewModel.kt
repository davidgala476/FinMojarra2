package com.finmojarra.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finmojarra.app.data.auth.AuthError
import com.finmojarra.app.data.auth.AuthRepository
import com.finmojarra.app.data.auth.AuthResult
import com.finmojarra.app.data.auth.GoogleTokenResult
import com.finmojarra.app.data.auth.SessionHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InicioUiState(
    val isLoading: Boolean = false,
    val error: AuthError? = null,
    val loggedIn: Boolean = false
)

/** Estado de la pantalla de inicio: solo maneja el botón "Continuar con Google". */
class InicioViewModel(
    private val repository: AuthRepository,
    private val sessionHolder: SessionHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    /** Se llama al pulsar el botón, antes de abrir el selector de cuentas de Google. */
    fun onGoogleStarted() {
        _uiState.update { it.copy(isLoading = true, error = null) }
    }

    /** Se llama cuando Google responde (o el usuario cierra el selector). */
    fun onGoogleResult(result: GoogleTokenResult) {
        when (result) {
            is GoogleTokenResult.Token -> viewModelScope.launch {
                when (val auth = repository.signInWithGoogle(result.idToken)) {
                    is AuthResult.Success -> {
                        sessionHolder.set(auth.data)
                        _uiState.update { it.copy(isLoading = false, loggedIn = true) }
                    }
                    is AuthResult.Failure ->
                        _uiState.update { it.copy(isLoading = false, error = auth.error) }
                }
            }
            GoogleTokenResult.Cancelled -> _uiState.update { it.copy(isLoading = false) }
            is GoogleTokenResult.Failure ->
                _uiState.update { it.copy(isLoading = false, error = result.error) }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
