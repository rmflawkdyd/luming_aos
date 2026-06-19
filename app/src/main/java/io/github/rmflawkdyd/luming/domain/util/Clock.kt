package io.github.rmflawkdyd.luming.domain.util

import io.github.rmflawkdyd.luming.domain.model.TimeBucket
import kotlinx.datetime.LocalDate

interface Clock {
    fun today(): LocalDate
    fun timeBucket(): TimeBucket
    fun dayOfWeekHash(): Int
    fun isRestHour(): Boolean
}
