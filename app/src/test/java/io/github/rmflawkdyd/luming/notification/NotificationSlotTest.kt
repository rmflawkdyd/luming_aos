package io.github.rmflawkdyd.luming.notification

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar

/**
 * 알림 슬롯 발화 시각 로직 검증 (spec §푸시: MORNING 09:00~10:59 / AFTERNOON 13:00~14:59 /
 * EVENING 19:00~20:59 랜덤 발화). computeTriggerMillis()는 java.util.Calendar + Random +
 * 인자 now 만 쓰므로 Android 런타임 없이 순수 JVM에서 검증한다.
 */
class NotificationSlotTest {

    private fun hourOf(millis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.HOUR_OF_DAY)

    /** millis가 속한 (연도, 연중 일자) 쌍 — 연 경계에서도 안전하게 날짜 비교용. */
    private fun ymd(millis: Long): Pair<Int, Int> =
        Calendar.getInstance().apply { timeInMillis = millis }
            .let { it.get(Calendar.YEAR) to it.get(Calendar.DAY_OF_YEAR) }

    /** 오늘 hourOfDay시 정각의 epoch millis. */
    private fun todayAt(hourOfDay: Int): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun tomorrowOf(millis: Long): Pair<Int, Int> =
        Calendar.getInstance().apply {
            timeInMillis = millis
            add(Calendar.DAY_OF_YEAR, 1)
        }.let { it.get(Calendar.YEAR) to it.get(Calendar.DAY_OF_YEAR) }

    @Test fun `슬롯 시간창 정의 - 시작 09_13_19시, 끝 11_15_21시(배타)`() {
        assertThat(NotificationSlot.MORNING.hour).isEqualTo(9)
        assertThat(NotificationSlot.MORNING.endHour).isEqualTo(11)
        assertThat(NotificationSlot.AFTERNOON.hour).isEqualTo(13)
        assertThat(NotificationSlot.AFTERNOON.endHour).isEqualTo(15)
        assertThat(NotificationSlot.EVENING.hour).isEqualTo(19)
        assertThat(NotificationSlot.EVENING.endHour).isEqualTo(21)
    }

    @Test fun `requestCode는 슬롯마다 고유`() {
        val codes = NotificationSlot.entries.map { it.requestCode }
        assertThat(codes).containsNoDuplicates()
        assertThat(codes).containsExactly(1001, 1002, 1003)
    }

    @Test fun `computeTriggerMillis - 발화 시각이 항상 슬롯 시간창 hour 이상 endHour 미만`() {
        val now = todayAt(6) // 모든 윈도우 시작 전
        for (slot in NotificationSlot.entries) {
            repeat(200) {
                val hour = hourOf(slot.computeTriggerMillis(now, alreadyFiredToday = false))
                assertThat(hour).isAtLeast(slot.hour)
                assertThat(hour).isLessThan(slot.endHour)
            }
        }
    }

    @Test fun `computeTriggerMillis - 항상 현재 이후의 미래 시각`() {
        val now = todayAt(6)
        for (slot in NotificationSlot.entries) {
            repeat(50) {
                assertThat(slot.computeTriggerMillis(now, alreadyFiredToday = false)).isGreaterThan(now)
            }
        }
    }

    @Test fun `computeTriggerMillis - 윈도우 시작 전이면 오늘 발화`() {
        val now = todayAt(6) // 09/13/19시 윈도우 모두 이후
        for (slot in NotificationSlot.entries) {
            repeat(50) {
                val trigger = slot.computeTriggerMillis(now, alreadyFiredToday = false)
                assertThat(ymd(trigger)).isEqualTo(ymd(now)) // 오늘
            }
        }
    }

    @Test fun `computeTriggerMillis - 오늘 이미 발화한 슬롯은 다음 날로 예약 (같은 시간대 중복 차단)`() {
        val now = todayAt(6)
        for (slot in NotificationSlot.entries) {
            repeat(50) {
                val trigger = slot.computeTriggerMillis(now, alreadyFiredToday = true)
                assertThat(ymd(trigger)).isEqualTo(tomorrowOf(now)) // 내일
                // 내일이어도 발화 시각은 여전히 슬롯 시간창 안.
                val hour = hourOf(trigger)
                assertThat(hour).isAtLeast(slot.hour)
                assertThat(hour).isLessThan(slot.endHour)
            }
        }
    }

    @Test fun `computeTriggerMillis - 윈도우 종료 후면 다음 날로 예약`() {
        val now = todayAt(23) // 모든 윈도우 종료 후
        for (slot in NotificationSlot.entries) {
            repeat(50) {
                val trigger = slot.computeTriggerMillis(now, alreadyFiredToday = false)
                assertThat(ymd(trigger)).isEqualTo(tomorrowOf(now)) // 내일
            }
        }
    }
}
