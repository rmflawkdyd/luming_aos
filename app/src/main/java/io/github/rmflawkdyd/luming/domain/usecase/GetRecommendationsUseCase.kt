package io.github.rmflawkdyd.luming.domain.usecase

import io.github.rmflawkdyd.luming.domain.model.ContextSnapshot
import io.github.rmflawkdyd.luming.domain.model.Recommendation
import io.github.rmflawkdyd.luming.domain.recommender.Recommender
import io.github.rmflawkdyd.luming.domain.repository.ActivityRepository
import javax.inject.Inject

class GetRecommendationsUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val recommender: Recommender,
) {
    suspend operator fun invoke(ctx: ContextSnapshot): List<Recommendation> {
        val activities = activityRepository.getActivities()
        return recommender.recommend(activities, ctx)
    }
}
