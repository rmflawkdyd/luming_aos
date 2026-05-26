package com.luming.data.recommender

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivityIndexTableDto(
    val version: String,
    @SerialName("pad_index") val padIndex: Int,
    val table: Map<String, Int>,
)
