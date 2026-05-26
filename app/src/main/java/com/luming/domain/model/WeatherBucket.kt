package com.luming.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class WeatherBucket {
    @SerialName("clear") CLEAR,
    @SerialName("cloudy") CLOUDY,
    @SerialName("rainy") RAINY,
    @SerialName("hot") HOT,
    @SerialName("cold") COLD,
    @SerialName("unknown") UNKNOWN,
}
