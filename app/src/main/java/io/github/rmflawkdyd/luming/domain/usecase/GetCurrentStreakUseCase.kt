package io.github.rmflawkdyd.luming.domain.usecase

import io.github.rmflawkdyd.luming.domain.model.Streak
import io.github.rmflawkdyd.luming.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentStreakUseCase @Inject constructor(
    private val streakRepository: StreakRepository,
) {
    operator fun invoke(): Flow<Streak> = streakRepository.getStreak()
}
