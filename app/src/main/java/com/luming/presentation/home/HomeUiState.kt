package com.luming.presentation.home

import com.luming.domain.model.Recommendation
import com.luming.domain.model.Streak
import com.luming.domain.model.TimeBucket
import com.luming.domain.model.WeatherBucket
import kotlinx.datetime.LocalDate

sealed interface HomeUiState {

    data object Empty : HomeUiState

    data class TimeOnly(
        val recommendations: List<Recommendation>,
        val streak: Streak,
        val date: LocalDate,
        val showCompletionOverlay: Boolean = false,
    ) : HomeUiState

    data class WeatherAware(
        val recommendations: List<Recommendation>,
        val streak: Streak,
        val date: LocalDate,
        val weatherBucket: WeatherBucket,
        val showCompletionOverlay: Boolean = false,
    ) : HomeUiState

    data object WeatherFailed : HomeUiState

    /** 시간대 완료 — ActivityCardList 대신 TimeSlotCompletedContent 표시 (NIGHT 제외) */
    data class CompletedSlot(
        val slot: TimeBucket,
        val streak: Streak,
        val date: LocalDate,
        val weatherBucket: WeatherBucket? = null,
        val showCompletionOverlay: Boolean = false,
    ) : HomeUiState

    /** 자정~04시 휴식 안내 화면 */
    data class RestPrompt(
        val streak: Streak,
        val date: LocalDate,
    ) : HomeUiState
}

internal fun HomeUiState.withOverlay(show: Boolean): HomeUiState = when (this) {
    is HomeUiState.WeatherAware -> copy(showCompletionOverlay = show)
    is HomeUiState.TimeOnly -> copy(showCompletionOverlay = show)
    is HomeUiState.CompletedSlot -> copy(showCompletionOverlay = show)
    else -> this
}

internal fun HomeUiState.showsOverlay(): Boolean = when (this) {
    is HomeUiState.WeatherAware -> showCompletionOverlay
    is HomeUiState.TimeOnly -> showCompletionOverlay
    is HomeUiState.CompletedSlot -> showCompletionOverlay
    else -> false
}
