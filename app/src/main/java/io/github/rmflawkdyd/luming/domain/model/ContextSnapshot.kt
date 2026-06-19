package io.github.rmflawkdyd.luming.domain.model

data class ContextSnapshot(
    val timeBucket: TimeBucket,
    val weatherBucket: WeatherBucket,
    val dayOfWeekHash: Int,
    val isPrecipitating: Boolean,
)
