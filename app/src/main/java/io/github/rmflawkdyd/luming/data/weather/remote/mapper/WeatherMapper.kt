package io.github.rmflawkdyd.luming.data.weather.remote.mapper

import io.github.rmflawkdyd.luming.data.weather.remote.dto.WeatherResponseDto
import io.github.rmflawkdyd.luming.domain.model.WeatherBucket
import io.github.rmflawkdyd.luming.domain.model.WeatherCondition
import io.github.rmflawkdyd.luming.domain.model.WeatherSnapshot
import kotlinx.datetime.Clock

object WeatherMapper {

    fun fromDto(dto: WeatherResponseDto): WeatherSnapshot {
        val condition = wmoToCondition(dto.current.weatherCode)
        val isPrecipitating = dto.current.precipitation > 0.0 ||
            condition in setOf(WeatherCondition.RAIN, WeatherCondition.SNOW, WeatherCondition.THUNDER)
        return WeatherSnapshot(
            condition = condition,
            temperatureC = dto.current.temperature2m,
            isPrecipitating = isPrecipitating,
            fetchedAt = Clock.System.now(),
        )
    }

    fun toWeatherBucket(snapshot: WeatherSnapshot?): WeatherBucket = when {
        snapshot == null -> WeatherBucket.UNKNOWN
        snapshot.condition in setOf(WeatherCondition.RAIN, WeatherCondition.SNOW, WeatherCondition.THUNDER) ->
            WeatherBucket.RAINY
        snapshot.temperatureC >= 28.0 -> WeatherBucket.HOT
        snapshot.temperatureC <= 5.0 -> WeatherBucket.COLD
        snapshot.condition == WeatherCondition.CLEAR -> WeatherBucket.CLEAR
        snapshot.condition in setOf(WeatherCondition.CLOUDY, WeatherCondition.FOG) -> WeatherBucket.CLOUDY
        else -> WeatherBucket.UNKNOWN
    }

    private fun wmoToCondition(code: Int): WeatherCondition = when (code) {
        0 -> WeatherCondition.CLEAR
        1, 2, 3 -> WeatherCondition.CLOUDY
        45, 48 -> WeatherCondition.FOG
        51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> WeatherCondition.RAIN
        71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOW
        95, 96, 99 -> WeatherCondition.THUNDER
        else -> WeatherCondition.UNKNOWN
    }
}
