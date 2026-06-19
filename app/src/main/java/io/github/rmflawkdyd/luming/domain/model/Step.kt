package io.github.rmflawkdyd.luming.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Step(
    val order: Int,
    val text: String,
)
