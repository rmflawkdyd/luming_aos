package com.luming.data.activity.local.dto

import com.luming.domain.model.Activity
import kotlinx.serialization.Serializable

@Serializable
data class ActivityLibraryDto(
    val version: String,
    val activities: List<Activity>,
)
