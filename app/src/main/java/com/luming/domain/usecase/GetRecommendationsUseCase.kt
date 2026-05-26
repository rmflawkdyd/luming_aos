package com.luming.domain.usecase

import com.luming.domain.model.ContextSnapshot
import com.luming.domain.model.Recommendation
import com.luming.domain.recommender.Recommender
import com.luming.domain.repository.ActivityRepository
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
