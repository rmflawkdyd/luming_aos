package io.github.rmflawkdyd.luming.notification

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar

/**
 * 알림 슬롯 발화 시각 로직 검증 (spec §푸시: MORNING 09:00~10:59 / AFTERNOON 13:00~14:59 /
 * EVENING 19:00~20:59 랜덤 발화). nextTriggerMillis()는 java.util.Calendar + Random +
 * System.currentTimeMillis() 만 쓰므로 Android 런타임 없이 순수 JVM에서 검증한다.
 */
class NotificationSlotTest {

    private fun hourOf(millis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.HOUR_OF_DAY)

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

    @Test fun `nextTriggerMillis - 발화 시각이 항상 슬롯 시간창 hour 이상 endHour 미만`() {
        // Random 분기를 충분히 커버하도록 슬롯별 반복 실행 (09:00~10:59 등 시간창 불변식).
        for (slot in NotificationSlot.entries) {
            repeat(200) {
                val hour = hourOf(slot.nextTriggerMillis())
                assertThat(hour).isAtLeast(slot.hour)
                assertThat(hour).isLessThan(slot.endHour)
            }
        }
    }

    @Test fun `nextTriggerMillis - 항상 현재 이후의 미래 시각`() {
        val before = System.currentTimeMillis()
        for (slot in NotificationSlot.entries) {
            repeat(50) {
                assertThat(slot.nextTriggerMillis()).isGreaterThan(before)
            }
        }
    }
}
