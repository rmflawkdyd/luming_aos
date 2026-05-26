package com.luming.domain.usecase

import com.luming.domain.model.Streak
import com.luming.domain.repository.StreakRepository
import com.luming.domain.util.Clock
import javax.inject.Inject

class MarkActivityCompleteUseCase @Inject constructor(
    private val streakRepository: StreakRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): Streak =
        streakRepository.markCompleted(clock.today())
}
