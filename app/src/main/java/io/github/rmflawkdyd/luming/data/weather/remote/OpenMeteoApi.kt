package io.github.rmflawkdyd.luming.data.weather.remote

import io.github.rmflawkdyd.luming.data.weather.remote.dto.WeatherResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,weather_code,precipitation",
        @Query("temperature_unit") temperatureUnit: String = "celsius",
        @Query("timezone") timezone: String = "auto",
    ): WeatherResponseDto
}
