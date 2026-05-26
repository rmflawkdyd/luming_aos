package com.luming.data.weather.cache

import com.luming.domain.model.WeatherSnapshot
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherInMemoryCache @Inject constructor() {

    private data class Entry(val snapshot: WeatherSnapshot, val expiresAt: Instant)

    private val cache = ConcurrentHashMap<String, Entry>()

    fun get(lat: Double, lon: Double): WeatherSnapshot? {
        val key = cacheKey(lat, lon)
        val entry = cache[key] ?: return null
        return if (Clock.System.now() < entry.expiresAt) {
            entry.snapshot
        } else {
            cache.remove(key)
            null
        }
    }

    fun put(lat: Double, lon: Double, snapshot: WeatherSnapshot) {
        cache[cacheKey(lat, lon)] = Entry(
            snapshot = snapshot,
            expiresAt = Clock.System.now() + 30.minutes,
        )
    }

    fun clear() = cache.clear()

    private fun cacheKey(lat: Double, lon: Double) =
        "weather/${lat.roundTo1Dp()},${lon.roundTo1Dp()}"

    private fun Double.roundTo1Dp(): Double = (this * 10).roundToInt() / 10.0
}
