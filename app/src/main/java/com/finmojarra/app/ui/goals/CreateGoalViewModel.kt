package com.finmojarra.app.ui.goals

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finmojarra.app.R
import com.finmojarra.app.data.goals.GoalWriteResult
import com.finmojarra.app.data.goals.GoalsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateGoalUiState(
    val name: String = "",
    /** Solo dígitos, sin puntos ni símbolos. */
    val amount: String = "",
    /** Solo dígitos: cuánto piensa ahorrar cada semana. */
    val weekly: String = "",
    /** Imagen elegida en la galería (opcional). */
    val imageUri: Uri? = null,
    @StringRes val nameError: Int? = null,
    @StringRes val amountError: Int? = null,
    @StringRes val weeklyError: Int? = null,
    /** Muestra el diálogo "Completa todos los datos para crear tu meta". */
    val showIncomplete: Boolean = false,
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
        _uiState.update { it.copy(amount = value.digitsOnly(), amountError = null) }
    }

    fun onWeeklyChange(value: String) {
        _uiState.update { it.copy(weekly = value.digitsOnly(), weeklyError = null) }
    }

    /** [uri] es null cuando el usuario cierra la galería sin elegir nada. */
    fun onImageSelected(uri: Uri?) {
        if (uri != null) _uiState.update { it.copy(imageUri = uri) }
    }

    fun removeImage() {
        _uiState.update { it.copy(imageUri = null) }
    }

    fun dismissIncomplete() {
        _uiState.update { it.copy(showIncomplete = false) }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun save() {
        val s = _uiState.value
        if (s.isSaving) return

        val nameError = if (s.name.isBlank()) R.string.error_field_required else null
        val amountError = positiveError(s.amount)
        val weeklyError = positiveError(s.weekly)

        if (nameError != null || amountError != null || weeklyError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    amountError = amountError,
                    weeklyError = weeklyError,
                    showIncomplete = true
                )
            }
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = repository.createGoal(
                name = s.name,
                targetAmount = s.amount.toLong().toDouble(),
                weeklyAmount = s.weekly.toLong().toDouble(),
                imageUri = s.imageUri
            )
            _uiState.update {
                when (result) {
                    GoalWriteResult.Success -> it.copy(isSaving = false, created = true)
                    is GoalWriteResult.Failure ->
                        it.copy(isSaving = false, message = result.reason.messageRes())
                }
            }
        }
    }

    @StringRes
    private fun positiveError(digits: String): Int? = when {
        digits.isBlank() -> R.string.error_field_required
        (digits.toLongOrNull() ?: 0L) <= 0L -> R.string.goals_error_amount
        else -> null
    }

    private fun String.digitsOnly() = filter { it.isDigit() }.trimStart('0').take(MAX_DIGITS)

    private companion object {
        const val MAX_NAME = 40
        const val MAX_DIGITS = 12
    }
}
