package com.luming.domain.repository

import com.luming.domain.model.WeatherSnapshot

interface WeatherRepository {
    suspend fun getWeather(lat: Double, lon: Double): WeatherSnapshot?
    suspend fun getLastCachedWeather(): WeatherSnapshot?
    fun clearCache()
    fun isWeatherCacheStale(): Boolean
}
