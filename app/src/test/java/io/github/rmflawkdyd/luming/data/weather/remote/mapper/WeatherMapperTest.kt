package io.github.rmflawkdyd.luming.data.weather.remote.mapper

import com.google.common.truth.Truth.assertThat
import io.github.rmflawkdyd.luming.domain.model.WeatherBucket
import io.github.rmflawkdyd.luming.domain.model.WeatherCondition
import io.github.rmflawkdyd.luming.domain.model.WeatherSnapshot
import kotlinx.datetime.Instant
import org.junit.Test

class WeatherMapperTest {

    private fun snap(
        condition: WeatherCondition,
        temp: Double = 20.0,
        isPrecipitating: Boolean = false,
    ) = WeatherSnapshot(
        condition = condition,
        temperatureC = temp,
        isPrecipitating = isPrecipitating,
        fetchedAt = Instant.fromEpochSeconds(0),
    )

    @Test fun `null은 UNKNOWN 반환`() {
        assertThat(WeatherMapper.toWeatherBucket(null)).isEqualTo(WeatherBucket.UNKNOWN)
    }

    @Test fun `RAIN 35도 - HOT 아닌 RAINY 반환`() {
        assertThat(WeatherMapper.toWeatherBucket(snap(WeatherCondition.RAIN, 35.0)))
            .isEqualTo(WeatherBucket.RAINY)
    }

    @Test fun `SNOW 35도 - HOT 아닌 RAINY 반환`() {
        assertThat(WeatherMapper.toWeatherBucket(snap(WeatherCondition.SNOW, 35.0)))
            .isEqualTo(WeatherBucket.RAINY)
    }

    @Test fun `THUNDER 35도 - HOT 아닌 RAINY 반환`() {
        assertThat(WeatherMapper.toWeatherBucket(snap(WeatherCondition.THUNDER, 35.0)))
            .isEqualTo(WeatherBucket.RAINY)
    }

    @Test fun `28도 정확히 - HOT 반환`() {
        assertThat(WeatherMapper.toWeatherBucket(snap(WeatherCondition.CLEAR, 28.0)))
            .isEqualTo(WeatherBucket.HOT)
    }

    @Test fun `27점9도 - HOT 아닌 CLEAR 반환`() {
        assertThat(WeatherMapper.toWeatherBucket(snap(WeatherCondition.CLEAR, 27.9)))
            .isEqualTo(WeatherBucket.CLEAR)
    }

    @Test fun `5도 정확히 - COLD 반환`() {
        assertThat(WeatherMapper.toWeatherBucket(snap(WeatherCondition.CLEAR, 5.0)))
            .isEqualTo(WeatherBucket.COLD)
    }

    @Test fun `5점1도 - COLD 아닌 CLEAR 반환`() {
        assertThat(WeatherMapper.toWeatherBucket(snap(WeatherCondition.CLEAR, 5.1)))
            .isEqualTo(WeatherBucket.CLEAR)
    }

    @Test fun `CLEAR 20도 - CLEAR 반환`() {
        assertThat(WeatherMapper.toWeatherBucket(snap(WeatherCondition.CLEAR, 20.0)))
            .isEqualTo(WeatherBucket.CLEAR)
    }

    @Test fun `CLOUDY - CLOUDY 반환`() {
        assertThat(WeatherMapper.toWeatherBucket(snap(WeatherCondition.CLOUDY)))
            .isEqualTo(WeatherBucket.CLOUDY)
    }

    @Test fun `FOG - CLOUDY 반환`() {
        assertThat(WeatherMapper.toWeatherBucket(snap(WeatherCondition.FOG)))
            .isEqualTo(WeatherBucket.CLOUDY)
    }

    @Test fun `UNKNOWN 컨디션 - UNKNOWN 버킷 반환`() {
        assertThat(WeatherMapper.toWeatherBucket(snap(WeatherCondition.UNKNOWN)))
            .isEqualTo(WeatherBucket.UNKNOWN)
    }
}
