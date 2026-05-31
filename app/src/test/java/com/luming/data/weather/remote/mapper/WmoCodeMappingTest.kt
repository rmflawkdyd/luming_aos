package com.luming.data.weather.remote.mapper

import com.google.common.truth.Truth.assertWithMessage
import com.luming.data.weather.remote.dto.CurrentWeatherDto
import com.luming.data.weather.remote.dto.WeatherResponseDto
import com.luming.domain.model.WeatherCondition
import org.junit.Test

class WmoCodeMappingTest {

    private fun dto(code: Int) = WeatherResponseDto(
        current = CurrentWeatherDto(temperature2m = 20.0, weatherCode = code, precipitation = 0.0),
    )

    @Test fun `WMO 코드 0-99 크래시 없음`() {
        (0..99).forEach { code ->
            WeatherMapper.fromDto(dto(code))
        }
    }

    @Test fun `WMO 0 - CLEAR`() {
        assertWithMessage("code 0").that(WeatherMapper.fromDto(dto(0)).condition)
            .isEqualTo(WeatherCondition.CLEAR)
    }

    @Test fun `WMO 1 2 3 - CLOUDY`() {
        listOf(1, 2, 3).forEach { code ->
            assertWithMessage("code $code").that(WeatherMapper.fromDto(dto(code)).condition)
                .isEqualTo(WeatherCondition.CLOUDY)
        }
    }

    @Test fun `WMO 45 48 - FOG`() {
        listOf(45, 48).forEach { code ->
            assertWithMessage("code $code").that(WeatherMapper.fromDto(dto(code)).condition)
                .isEqualTo(WeatherCondition.FOG)
        }
    }

    @Test fun `WMO 강수 코드 - RAIN`() {
        listOf(51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82).forEach { code ->
            assertWithMessage("code $code").that(WeatherMapper.fromDto(dto(code)).condition)
                .isEqualTo(WeatherCondition.RAIN)
        }
    }

    @Test fun `WMO 눈 코드 - SNOW`() {
        listOf(71, 73, 75, 77, 85, 86).forEach { code ->
            assertWithMessage("code $code").that(WeatherMapper.fromDto(dto(code)).condition)
                .isEqualTo(WeatherCondition.SNOW)
        }
    }

    @Test fun `WMO 95 96 99 - THUNDER`() {
        listOf(95, 96, 99).forEach { code ->
            assertWithMessage("code $code").that(WeatherMapper.fromDto(dto(code)).condition)
                .isEqualTo(WeatherCondition.THUNDER)
        }
    }

    @Test fun `매핑 없는 WMO 코드 - UNKNOWN`() {
        val mapped = setOf(0, 1, 2, 3, 45, 48, 51, 53, 55, 56, 57, 61, 63, 65, 66, 67,
            71, 73, 75, 77, 80, 81, 82, 85, 86, 95, 96, 99)
        (0..99).filter { it !in mapped }.forEach { code ->
            assertWithMessage("code $code").that(WeatherMapper.fromDto(dto(code)).condition)
                .isEqualTo(WeatherCondition.UNKNOWN)
        }
    }
}
