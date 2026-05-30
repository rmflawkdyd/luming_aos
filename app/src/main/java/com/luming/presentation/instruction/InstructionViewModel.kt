package com.luming.presentation.instruction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luming.data.slotcompletion.SlotCompletionStore
import com.luming.domain.model.TimeBucket
import com.luming.domain.repository.ActivityRepository
import com.luming.domain.usecase.MarkActivityCompleteUseCase
import com.luming.domain.util.Clock
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
    private val slotStore: SlotCompletionStore,
    private val clock: Clock,
) : ViewModel() {

    private val activityId: String = checkNotNull(savedStateHandle["activityId"])

    private val _uiState = MutableStateFlow<InstructionUiState?>(null)
    val uiState: StateFlow<InstructionUiState?> = _uiState.asStateFlow()

    // StartButton 탭 시점의 TimeBucket 동결 (ADR-011, race condition 방지)
    private var startedSlot: TimeBucket? = null

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
        startedSlot = clock.timeBucket()
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

    /** BackButton / 중단 — startedSlot 명시적 리셋 (ViewModel 재사용 방어, AC-S13) */
    fun onAbort() {
        startedSlot = null
        _uiState.update { it?.copy(timerStartedAt = null) }
    }

    private fun doComplete(onNavigate: () -> Unit) {
        _uiState.update { it?.copy(isCompleting = true) }
        viewModelScope.launch {
            markActivityComplete()
            // 시작 슬롯 기준으로 완료 기록 — NIGHT는 제외 (AC-S6, AC-S9, AC-S10)
            startedSlot?.takeIf { it != TimeBucket.NIGHT }?.let { slot ->
                slotStore.markCompleted(slot, clock.today())
            }
            onNavigate()
        }
    }
}
