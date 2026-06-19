package io.github.rmflawkdyd.luming.domain.repository

import io.github.rmflawkdyd.luming.domain.model.Streak
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface StreakRepository {
    fun getStreak(): Flow<Streak>
    suspend fun markCompleted(today: LocalDate): Streak
}
