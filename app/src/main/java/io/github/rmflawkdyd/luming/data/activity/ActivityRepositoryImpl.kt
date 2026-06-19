package io.github.rmflawkdyd.luming.data.activity

import io.github.rmflawkdyd.luming.data.activity.local.ActivityLocalDataSource
import io.github.rmflawkdyd.luming.domain.model.Activity
import io.github.rmflawkdyd.luming.domain.repository.ActivityRepository
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val localDataSource: ActivityLocalDataSource,
) : ActivityRepository {
    override suspend fun getActivities(): List<Activity> =
        localDataSource.getActivities()
}
