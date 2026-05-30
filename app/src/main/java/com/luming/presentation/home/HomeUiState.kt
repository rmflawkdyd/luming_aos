package com.luming.presentation.home

import com.luming.domain.model.Recommendation
import com.luming.domain.model.Streak
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

    data object LocationFailed : HomeUiState

    data object WeatherFailed : HomeUiState
}

internal fun HomeUiState.withOverlay(show: Boolean): HomeUiState = when (this) {
    is HomeUiState.WeatherAware -> copy(showCompletionOverlay = show)
    is HomeUiState.TimeOnly -> copy(showCompletionOverlay = show)
    else -> this
}
