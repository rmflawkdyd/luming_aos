package com.luming.domain.usecase

import com.luming.domain.model.Streak
import com.luming.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentStreakUseCase @Inject constructor(
    private val streakRepository: StreakRepository,
) {
    operator fun invoke(): Flow<Streak> = streakRepository.getStreak()
}
