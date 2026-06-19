package io.github.rmflawkdyd.luming.domain.model

import kotlinx.datetime.Instant

data class WeatherSnapshot(
    val condition: WeatherCondition,
    val temperatureC: Double,
    val isPrecipitating: Boolean,
    val fetchedAt: Instant,
)
