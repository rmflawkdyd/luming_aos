package com.luming.data.streak

import com.luming.data.streak.local.StreakDataStore
import com.luming.domain.model.Streak
import com.luming.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class StreakRepositoryImpl @Inject constructor(
    private val dataStore: StreakDataStore,
) : StreakRepository {
    override fun getStreak(): Flow<Streak> = dataStore.getStreak()
    override suspend fun markCompleted(today: LocalDate): Streak = dataStore.markCompleted(today)
}
