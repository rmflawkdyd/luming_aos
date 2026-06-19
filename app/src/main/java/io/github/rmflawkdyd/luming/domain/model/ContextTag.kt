package io.github.rmflawkdyd.luming.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ContextTag {
    @SerialName("morning") MORNING,
    @SerialName("afternoon") AFTERNOON,
    @SerialName("evening") EVENING,
    @SerialName("night") NIGHT,
    @SerialName("hot") HOT,
    @SerialName("cold") COLD,
    @SerialName("rainy") RAINY,
    @SerialName("clear") CLEAR,
    @SerialName("cloudy") CLOUDY,
    @SerialName("indoor") INDOOR,
    @SerialName("outdoor") OUTDOOR,
}
