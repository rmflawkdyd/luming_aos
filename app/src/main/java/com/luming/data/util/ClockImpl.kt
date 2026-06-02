package com.luming.data.util

import com.luming.domain.model.TimeBucket
import com.luming.domain.util.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.time.ZonedDateTime
import javax.inject.Inject

class ClockImpl @Inject constructor() : Clock {

    override fun today(): LocalDate =
        java.time.LocalDate.now().toKotlinLocalDate()

    override fun timeBucket(): TimeBucket {
        val hour = ZonedDateTime.now().hour
        return when {
            hour in 5..11 -> TimeBucket.MORNING
            hour in 12..16 -> TimeBucket.AFTERNOON
            hour in 17..20 -> TimeBucket.EVENING
            else -> TimeBucket.NIGHT
        }
    }

    override fun isRestHour(): Boolean = ZonedDateTime.now().hour in 0..4

    override fun dayOfWeekHash(): Int =
        today().dayOfWeek.value % 7
}
