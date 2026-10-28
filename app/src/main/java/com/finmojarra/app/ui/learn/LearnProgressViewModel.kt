package com.finmojarra.app.ui.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finmojarra.app.data.learn.LearningProgressRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Pasos hechos de un curso y la forma de marcar uno nuevo. */
class LearnProgressViewModel(
    private val repository: LearningProgressRepository,
    private val pathId: String
) : ViewModel() {

    val steps: StateFlow<Set<String>> = repository.observeSteps(pathId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun complete(stepId: String) = repository.markStepDone(pathId, stepId)
}
