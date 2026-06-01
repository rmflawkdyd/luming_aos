package com.luming.notification

import java.util.Calendar

enum class NotificationSlot(val requestCode: Int, val hour: Int) {
    MORNING(requestCode = 1001, hour = 8),
    AFTERNOON(requestCode = 1002, hour = 12),
    EVENING(requestCode = 1003, hour = 17);

    fun nextTriggerMillis(): Long {
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= System.currentTimeMillis()) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }
}
