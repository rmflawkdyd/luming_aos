package com.luming.data.activity.local

import android.content.Context
import com.luming.data.activity.local.dto.ActivityLibraryDto
import com.luming.domain.model.Activity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityLocalDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val activities: List<Activity> by lazy {
        val text = context.assets.open("activities.v1.json").bufferedReader().readText()
        json.decodeFromString<ActivityLibraryDto>(text).activities
    }

    suspend fun getActivities(): List<Activity> = withContext(Dispatchers.IO) { activities }
}
