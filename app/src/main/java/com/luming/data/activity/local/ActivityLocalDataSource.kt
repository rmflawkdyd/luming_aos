package com.luming.data.activity.local

import android.content.Context
import com.luming.data.activity.local.dto.ActivityLibraryDto
import com.luming.domain.model.Activity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityLocalDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var cached: List<Activity>? = null

    fun getActivities(): List<Activity> {
        return cached ?: run {
            val text = context.assets.open("activities.v1.json").bufferedReader().readText()
            val dto = json.decodeFromString<ActivityLibraryDto>(text)
            dto.activities.also { cached = it }
        }
    }
}
