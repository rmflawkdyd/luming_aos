package com.luming.data.activity

import com.luming.data.activity.local.ActivityLocalDataSource
import com.luming.domain.model.Activity
import com.luming.domain.repository.ActivityRepository
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val localDataSource: ActivityLocalDataSource,
) : ActivityRepository {
    override suspend fun getActivities(): List<Activity> =
        localDataSource.getActivities()
}
