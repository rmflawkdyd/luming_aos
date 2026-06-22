package io.github.rmflawkdyd.luming.notification

import java.util.Calendar
import kotlin.random.Random

enum class NotificationSlot(val requestCode: Int, val hour: Int, val endHour: Int) {
    MORNING(requestCode = 1001, hour = 9, endHour = 11),
    AFTERNOON(requestCode = 1002, hour = 13, endHour = 15),
    EVENING(requestCode = 1003, hour = 19, endHour = 21);

    /**
     * 다음 발화 시각(epoch millis). 슬롯 시간창 [hour, endHour) 안의 랜덤 시각으로 잡되,
     * 같은 슬롯이 하루 두 번 울리지 않도록 다음 경우엔 다음 날로 미룬다.
     *  - [alreadyFiredToday] == true : 오늘 이미 발화 → 무조건 내일.
     *  - target <= [now]             : 오늘 윈도우가 이미 지남 → 내일.
     * 그 외에는 오늘 윈도우 내 미래 시각으로 예약한다.
     *
     * [now]/[random]을 인자로 받아 Android 런타임 없이 순수 JVM에서 검증 가능하다.
     */
    fun computeTriggerMillis(
        now: Long,
        alreadyFiredToday: Boolean,
        random: Random = Random,
    ): Long {
        val randomMinuteOffset = random.nextInt(0, (endHour - hour) * 60)
        val target = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, randomMinuteOffset)
        }
        if (alreadyFiredToday || target.timeInMillis <= now) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }
}
