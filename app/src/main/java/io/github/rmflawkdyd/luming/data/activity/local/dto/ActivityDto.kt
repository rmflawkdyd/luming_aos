package io.github.rmflawkdyd.luming.data.activity.local.dto

import io.github.rmflawkdyd.luming.domain.model.Activity
import kotlinx.serialization.Serializable

@Serializable
data class ActivityLibraryDto(
    val version: String,
    val activities: List<Activity>,
)
