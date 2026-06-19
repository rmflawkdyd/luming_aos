package io.github.rmflawkdyd.luming.domain.model

/**
 * Runtime-only weather classification used for rule scoring + rationale text.
 *
 * Intentionally NOT @Serializable: WeatherBucket is never persisted or JSON-encoded.
 * Weather is cached as [WeatherCondition] + primitives in WeatherDataStore and mapped
 * to a bucket at runtime via WeatherMapper, so there is no serialization path to map.
 */
enum class WeatherBucket {
    CLEAR,
    CLOUDY,
    RAINY,
    HOT,
    COLD,
    UNKNOWN,
}
