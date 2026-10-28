package com.finmojarra.app.ui.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finmojarra.app.data.learn.LearningProgressRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Pasos hechos de todos los cursos (por id de curso) y la forma de marcar uno nuevo. */
class LearnProgressViewModel(
    private val repository: LearningProgressRepository
) : ViewModel() {

    val steps: StateFlow<Map<String, Set<String>>> = repository.observeAllSteps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun complete(pathId: String, stepId: String) = repository.markStepDone(pathId, stepId)
}
