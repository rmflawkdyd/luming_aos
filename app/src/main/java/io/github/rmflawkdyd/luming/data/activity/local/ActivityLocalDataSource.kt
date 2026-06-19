package io.github.rmflawkdyd.luming.data.activity.local

import android.content.Context
import io.github.rmflawkdyd.luming.data.activity.local.dto.ActivityLibraryDto
import io.github.rmflawkdyd.luming.domain.model.Activity
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

    // Activity content (name + step text) is localized per language. Assets are not
    // resolved through the resource qualifier system, so we pick the file explicitly
    // from the current configuration locale. Falls back to the Korean library when no
    // localized asset exists for the active language.
    private fun assetNameForLocale(): String {
        val language = context.resources.configuration.locales[0].language
        return when (language) {
            "en" -> "activities.en.v1.json"
            else -> "activities.v1.json"
        }
    }

    private fun loadActivities(): List<Activity> {
        val assetName = assetNameForLocale()
        val text = context.assets.open(assetName).bufferedReader().readText()
        return json.decodeFromString<ActivityLibraryDto>(text).activities
    }

    suspend fun getActivities(): List<Activity> = withContext(Dispatchers.IO) { loadActivities() }
}
