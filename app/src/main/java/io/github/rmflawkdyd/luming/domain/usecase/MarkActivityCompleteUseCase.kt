package io.github.rmflawkdyd.luming.domain.usecase

import io.github.rmflawkdyd.luming.domain.model.Streak
import io.github.rmflawkdyd.luming.domain.repository.StreakRepository
import io.github.rmflawkdyd.luming.domain.util.Clock
import javax.inject.Inject

class MarkActivityCompleteUseCase @Inject constructor(
    private val streakRepository: StreakRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): Streak =
        streakRepository.markCompleted(clock.today())
}
