package com.luming.domain.repository

import com.luming.domain.model.Streak
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface StreakRepository {
    fun getStreak(): Flow<Streak>
    suspend fun markCompleted(today: LocalDate): Streak
}
