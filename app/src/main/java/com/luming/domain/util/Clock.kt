package com.luming.domain.util

import com.luming.domain.model.TimeBucket
import kotlinx.datetime.LocalDate

interface Clock {
    fun today(): LocalDate
    fun timeBucket(): TimeBucket
    fun dayOfWeekHash(): Int
}
