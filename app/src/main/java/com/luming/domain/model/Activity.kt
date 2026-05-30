package com.luming.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Activity(
    val id: String,
    val name: String,
    val category: Category,
    @SerialName("duration_min") val durationMin: Int,
    val steps: List<Step>,
    @SerialName("context_tags") val contextTags: Set<ContextTag>,
)

@Serializable
enum class Category {
    @SerialName("stretch") STRETCH,
    @SerialName("meditation") MEDITATION,
    @SerialName("breathing") BREATHING,
    @SerialName("walk") WALK,
    @SerialName("focus") FOCUS,
    @SerialName("movement") MOVEMENT,
    @SerialName("rest") REST,
}
