package com.luming.presentation.instruction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luming.domain.repository.ActivityRepository
import com.luming.domain.usecase.MarkActivityCompleteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InstructionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val markActivityComplete: MarkActivityCompleteUseCase,
) : ViewModel() {

    private val activityId: String = checkNotNull(savedStateHandle["activityId"])

    private val _uiState = MutableStateFlow<InstructionUiState?>(null)
    val uiState: StateFlow<InstructionUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val activity = activityRepository.getActivities().first { it.id == activityId }
            _uiState.value = InstructionUiState(activity)
        }
    }

    fun goToNextStep() {
        _uiState.update { state ->
            if (state != null && !state.isLastStep) state.copy(currentStepIndex = state.currentStepIndex + 1)
            else state
        }
    }

    fun goToPreviousStep() {
        _uiState.update { state ->
            if (state != null && !state.isFirstStep) state.copy(currentStepIndex = state.currentStepIndex - 1)
            else state
        }
    }

    fun startTimer() {
        _uiState.update { it?.copy(timerStartedAt = System.currentTimeMillis()) }
    }

    fun requestComplete(elapsedMs: Long, onNavigate: () -> Unit) {
        val state = _uiState.value ?: return
        val thresholdMs = (state.activity.durationMin * 60_000L * 0.8).toLong()
        if (elapsedMs >= thresholdMs) {
            doComplete(onNavigate)
        } else {
            _uiState.update { it?.copy(showTimerWarning = true) }
        }
    }

    fun dismissWarning() {
        _uiState.update { it?.copy(showTimerWarning = false) }
    }

    fun confirmComplete(onNavigate: () -> Unit) {
        _uiState.update { it?.copy(showTimerWarning = false) }
        doComplete(onNavigate)
    }

    private fun doComplete(onNavigate: () -> Unit) {
        _uiState.update { it?.copy(isCompleting = true) }
        viewModelScope.launch {
            markActivityComplete()
            onNavigate()
        }
    }
}
