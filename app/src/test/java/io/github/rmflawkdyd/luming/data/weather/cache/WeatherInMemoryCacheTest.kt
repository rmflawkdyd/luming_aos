package io.github.rmflawkdyd.luming.data.weather.cache

import com.google.common.truth.Truth.assertThat
import io.github.rmflawkdyd.luming.domain.model.WeatherCondition
import io.github.rmflawkdyd.luming.domain.model.WeatherSnapshot
import kotlinx.datetime.Instant
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.Clock as KtClock

/**
 * 날씨 인메모리 캐시 검증. 좌표를 소수점 1자리로 반올림해 키를 만드는 동작
 * (coarse-location 1자리 반올림 제약 직결)과, 시각을 주입해 검증하는 30분 TTL 만료를 다룬다.
 */
class WeatherInMemoryCacheTest {

    /** 손으로 시각을 돌릴 수 있는 가짜 시계. */
    private class MutableClock(var instant: Instant) : KtClock {
        override fun now(): Instant = instant
    }

    private val t0 = Instant.fromEpochSeconds(1_000_000)

    private fun snap(temp: Double = 20.0) = WeatherSnapshot(
        condition = WeatherCondition.CLEAR,
        temperatureC = temp,
        isPrecipitating = false,
        fetchedAt = Instant.fromEpochSeconds(0),
    )

    @Test fun `빈 캐시 - get은 null`() {
        val cache = WeatherInMemoryCache()
        assertThat(cache.get(37.5, 127.0)).isNull()
    }

    @Test fun `put 후 get - 동일 스냅샷 반환`() {
        val cache = WeatherInMemoryCache()
        val s = snap()
        cache.put(37.5, 127.0, s)
        assertThat(cache.get(37.5, 127.0)).isEqualTo(s)
    }

    @Test fun `같은 0_1 격자로 반올림되는 좌표는 캐시 적중`() {
        val cache = WeatherInMemoryCache()
        val s = snap()
        cache.put(37.54, 127.04, s)                  // → 37.5, 127.0
        assertThat(cache.get(37.49, 126.96)).isEqualTo(s)  // → 37.5, 127.0
    }

    @Test fun `1자리 반올림 격자가 다르면 캐시 미스`() {
        val cache = WeatherInMemoryCache()
        cache.put(37.5, 127.0, snap())
        assertThat(cache.get(37.6, 127.0)).isNull()
    }

    @Test fun `hasAnyValid - put 전 false, put 후 true`() {
        val cache = WeatherInMemoryCache()
        assertThat(cache.hasAnyValid()).isFalse()
        cache.put(37.5, 127.0, snap())
        assertThat(cache.hasAnyValid()).isTrue()
    }

    @Test fun `clear - 모든 엔트리 제거`() {
        val cache = WeatherInMemoryCache()
        cache.put(37.5, 127.0, snap())
        cache.clear()
        assertThat(cache.get(37.5, 127.0)).isNull()
        assertThat(cache.hasAnyValid()).isFalse()
    }

    // --- 30분 TTL 만료 (시각 주입으로 결정적 검증) ---

    @Test fun `put 후 29분 - 아직 유효`() {
        val clock = MutableClock(t0)
        val cache = WeatherInMemoryCache(clock)
        cache.put(37.5, 127.0, snap())
        clock.instant = t0 + 29.minutes
        assertThat(cache.get(37.5, 127.0)).isNotNull()
    }

    @Test fun `put 후 정확히 30분 - 만료(경계는 배타)`() {
        val clock = MutableClock(t0)
        val cache = WeatherInMemoryCache(clock)
        cache.put(37.5, 127.0, snap())
        clock.instant = t0 + 30.minutes
        assertThat(cache.get(37.5, 127.0)).isNull()
    }

    @Test fun `put 후 31분 - 만료되어 null 및 hasAnyValid false`() {
        val clock = MutableClock(t0)
        val cache = WeatherInMemoryCache(clock)
        cache.put(37.5, 127.0, snap())
        clock.instant = t0 + 31.minutes
        assertThat(cache.get(37.5, 127.0)).isNull()
        assertThat(cache.hasAnyValid()).isFalse()
    }
}
