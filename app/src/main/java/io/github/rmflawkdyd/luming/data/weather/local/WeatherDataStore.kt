package io.github.rmflawkdyd.luming.data.weather.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.rmflawkdyd.luming.domain.model.WeatherCondition
import io.github.rmflawkdyd.luming.domain.model.WeatherSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class WeatherDataStore @Inject constructor(
    @Named("weather") private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private val KEY_CONDITION = stringPreferencesKey("condition")
        private val KEY_TEMP = floatPreferencesKey("temp_c")
        private val KEY_IS_PRECIPITATING = booleanPreferencesKey("is_precipitating")
        private val KEY_FETCHED_AT = longPreferencesKey("fetched_at_ms")
        private const val MAX_AGE_MS = 30 * 60 * 1000L
    }

    suspend fun getLastWeather(): WeatherSnapshot? {
        val prefs = dataStore.data.first()
        val fetchedAtMs = prefs[KEY_FETCHED_AT] ?: return null
        if (System.currentTimeMillis() - fetchedAtMs > MAX_AGE_MS) return null
        val condition = prefs[KEY_CONDITION]
            ?.let { runCatching { WeatherCondition.valueOf(it) }.getOrNull() }
            ?: return null
        val temp = prefs[KEY_TEMP] ?: return null
        val isPrecipitating = prefs[KEY_IS_PRECIPITATING] ?: false
        return WeatherSnapshot(
            condition = condition,
            temperatureC = temp.toDouble(),
            isPrecipitating = isPrecipitating,
            fetchedAt = Instant.fromEpochMilliseconds(fetchedAtMs),
        )
    }

    suspend fun save(snapshot: WeatherSnapshot) {
        dataStore.edit { prefs ->
            prefs[KEY_CONDITION] = snapshot.condition.name
            prefs[KEY_TEMP] = snapshot.temperatureC.toFloat()
            prefs[KEY_IS_PRECIPITATING] = snapshot.isPrecipitating
            prefs[KEY_FETCHED_AT] = snapshot.fetchedAt.toEpochMilliseconds()
        }
    }
}
