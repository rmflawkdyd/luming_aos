package io.github.rmflawkdyd.luming.domain.model

import kotlinx.datetime.LocalDate

data class Streak(
    val currentCount: Int,
    val lastCompletedDate: LocalDate?,
    val last7Days: List<Boolean>,
)
