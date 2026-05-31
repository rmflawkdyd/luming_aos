package com.luming.data.weather

import com.luming.data.weather.cache.WeatherInMemoryCache
import com.luming.data.weather.local.WeatherDataStore
import com.luming.data.weather.remote.OpenMeteoApi
import com.luming.data.weather.remote.mapper.WeatherMapper
import com.luming.domain.model.WeatherSnapshot
import com.luming.domain.repository.WeatherRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: OpenMeteoApi,
    private val cache: WeatherInMemoryCache,
    private val dataStore: WeatherDataStore,
) : WeatherRepository {

    override suspend fun getWeather(lat: Double, lon: Double): WeatherSnapshot? = try {
        cache.get(lat, lon) ?: run {
            val dto = api.getForecast(lat, lon)
            val snapshot = WeatherMapper.fromDto(dto)
            cache.put(lat, lon, snapshot)
            dataStore.save(snapshot)
            snapshot
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    override suspend fun getLastCachedWeather(): WeatherSnapshot? = dataStore.getLastWeather()

    override fun clearCache() = cache.clear()

    override fun isWeatherCacheStale(): Boolean = !cache.hasAnyValid()
}
