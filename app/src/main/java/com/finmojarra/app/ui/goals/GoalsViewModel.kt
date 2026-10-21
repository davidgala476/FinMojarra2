package com.finmojarra.app.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finmojarra.app.data.goals.Goal
import com.finmojarra.app.data.goals.GoalsRepository
import com.finmojarra.app.data.goals.GoalsResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GoalsUiState(
    val isLoading: Boolean = true,
    val goals: List<Goal> = emptyList(),
    val hasError: Boolean = false
)

class GoalsViewModel(
    private val repository: GoalsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    private var job: Job? = null

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        job?.cancel()
        _uiState.update { it.copy(isLoading = true, hasError = false) }
        job = viewModelScope.launch {
            repository.observeGoals().collect { result ->
                _uiState.value = when (result) {
                    is GoalsResult.Success -> GoalsUiState(isLoading = false, goals = result.goals)
                    GoalsResult.Failure -> GoalsUiState(isLoading = false, hasError = true)
                }
            }
        }
    }
}
