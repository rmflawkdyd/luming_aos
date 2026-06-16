package com.luming.data.util

import com.google.common.truth.Truth.assertThat
import com.luming.domain.model.TimeBucket
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Test
import kotlinx.datetime.Clock as KtClock

/**
 * ClockImpl 검증. 시각/타임존을 주입받게 리팩터링되어, 고정 시각을 꽂아
 * 시간→버킷 경계값과 isRestHour(00~04시)를 결정적으로 검증한다.
 */
class ClockImplTest {

    private val tz = TimeZone.UTC

    private fun fixedClock(instant: Instant) = object : KtClock {
        override fun now(): Instant = instant
    }

    /** 2026-06-16(화) [hour]시에 고정된 ClockImpl. */
    private fun clockAt(hour: Int): ClockImpl {
        val instant = LocalDateTime(2026, 6, 16, hour, 0, 0).toInstant(tz)
        return ClockImpl(fixedClock(instant), tz)
    }

    // --- timeBucket 경계값 (5..11 M / 12..16 A / 17..20 E / 그 외 N) ---

    @Test fun `4시는 NIGHT (MORNING 시작 직전)`() {
        assertThat(clockAt(4).timeBucket()).isEqualTo(TimeBucket.NIGHT)
    }

    @Test fun `5시는 MORNING 시작`() {
        assertThat(clockAt(5).timeBucket()).isEqualTo(TimeBucket.MORNING)
    }

    @Test fun `11시는 MORNING 끝`() {
        assertThat(clockAt(11).timeBucket()).isEqualTo(TimeBucket.MORNING)
    }

    @Test fun `12시는 AFTERNOON 시작`() {
        assertThat(clockAt(12).timeBucket()).isEqualTo(TimeBucket.AFTERNOON)
    }

    @Test fun `16시는 AFTERNOON 끝`() {
        assertThat(clockAt(16).timeBucket()).isEqualTo(TimeBucket.AFTERNOON)
    }

    @Test fun `17시는 EVENING 시작`() {
        assertThat(clockAt(17).timeBucket()).isEqualTo(TimeBucket.EVENING)
    }

    @Test fun `20시는 EVENING 끝`() {
        assertThat(clockAt(20).timeBucket()).isEqualTo(TimeBucket.EVENING)
    }

    @Test fun `21시는 NIGHT 시작`() {
        assertThat(clockAt(21).timeBucket()).isEqualTo(TimeBucket.NIGHT)
    }

    @Test fun `0시는 NIGHT`() {
        assertThat(clockAt(0).timeBucket()).isEqualTo(TimeBucket.NIGHT)
    }

    // --- isRestHour 경계값 (00~04시) ---

    @Test fun `0시는 휴식시간`() {
        assertThat(clockAt(0).isRestHour()).isTrue()
    }

    @Test fun `4시는 휴식시간 끝`() {
        assertThat(clockAt(4).isRestHour()).isTrue()
    }

    @Test fun `5시는 휴식시간 아님`() {
        assertThat(clockAt(5).isRestHour()).isFalse()
    }

    @Test fun `23시는 휴식시간 아님`() {
        assertThat(clockAt(23).isRestHour()).isFalse()
    }

    @Test fun `휴식시간(00~04시)은 모두 NIGHT 버킷 부분집합`() {
        for (hour in 0..4) {
            val clock = clockAt(hour)
            assertThat(clock.isRestHour()).isTrue()
            assertThat(clock.timeBucket()).isEqualTo(TimeBucket.NIGHT)
        }
    }

    // --- today / dayOfWeekHash ---

    @Test fun `today는 주입된 시각의 날짜를 반영`() {
        assertThat(clockAt(10).today()).isEqualTo(LocalDate(2026, 6, 16))
    }

    @Test fun `dayOfWeekHash는 다음 날 1 증가(mod 7)`() {
        val tue = LocalDateTime(2026, 6, 16, 10, 0, 0).toInstant(tz)  // 화
        val wed = LocalDateTime(2026, 6, 17, 10, 0, 0).toInstant(tz)  // 수
        val tueHash = ClockImpl(fixedClock(tue), tz).dayOfWeekHash()
        val wedHash = ClockImpl(fixedClock(wed), tz).dayOfWeekHash()
        assertThat(wedHash).isEqualTo((tueHash + 1) % 7)
        assertThat(tueHash).isAtLeast(0)
        assertThat(tueHash).isAtMost(6)
    }
}
