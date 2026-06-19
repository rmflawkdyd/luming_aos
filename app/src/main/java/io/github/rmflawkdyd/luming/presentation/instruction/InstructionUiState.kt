package io.github.rmflawkdyd.luming.presentation.instruction

import io.github.rmflawkdyd.luming.domain.model.Activity
import io.github.rmflawkdyd.luming.domain.model.Step

data class InstructionUiState(
    val activity: Activity,
    val currentStepIndex: Int = 0,
    val timerStartedAt: Long? = null,
    val isCompleting: Boolean = false,
    val showTimerWarning: Boolean = false,
    val showAbortWarning: Boolean = false,
    val navigateBack: Boolean = false,
) {
    private val sortedSteps: List<Step> get() = activity.steps.sortedBy { it.order }
    val currentStep: Step get() = sortedSteps[currentStepIndex]
    val totalSteps: Int get() = activity.steps.size
    val isLastStep: Boolean get() = currentStepIndex == totalSteps - 1
    val isFirstStep: Boolean get() = currentStepIndex == 0
    val isTimerRunning: Boolean get() = timerStartedAt != null
}
