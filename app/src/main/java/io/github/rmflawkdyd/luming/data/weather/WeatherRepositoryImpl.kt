package io.github.rmflawkdyd.luming.data.weather

import io.github.rmflawkdyd.luming.data.weather.cache.WeatherInMemoryCache
import io.github.rmflawkdyd.luming.data.weather.local.WeatherDataStore
import io.github.rmflawkdyd.luming.data.weather.remote.OpenMeteoApi
import io.github.rmflawkdyd.luming.data.weather.remote.mapper.WeatherMapper
import io.github.rmflawkdyd.luming.domain.model.WeatherSnapshot
import io.github.rmflawkdyd.luming.domain.repository.WeatherRepository
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
