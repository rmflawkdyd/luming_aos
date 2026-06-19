package io.github.rmflawkdyd.luming.domain.repository

import io.github.rmflawkdyd.luming.domain.model.WeatherSnapshot

interface WeatherRepository {
    suspend fun getWeather(lat: Double, lon: Double): WeatherSnapshot?
    suspend fun getLastCachedWeather(): WeatherSnapshot?
    fun clearCache()
    fun isWeatherCacheStale(): Boolean
}
