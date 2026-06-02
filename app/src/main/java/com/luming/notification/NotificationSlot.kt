package com.luming.notification

import java.util.Calendar
import kotlin.random.Random

enum class NotificationSlot(val requestCode: Int, val hour: Int, val endHour: Int) {
    MORNING(requestCode = 1001, hour = 9, endHour = 11),
    AFTERNOON(requestCode = 1002, hour = 13, endHour = 15),
    EVENING(requestCode = 1003, hour = 19, endHour = 21);

    fun nextTriggerMillis(): Long {
        val randomMinuteOffset = Random.nextInt(0, (endHour - hour) * 60)
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, randomMinuteOffset)
        }
        if (target.timeInMillis <= System.currentTimeMillis()) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }
}
