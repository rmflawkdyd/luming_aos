package com.luming.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager: AlarmManager = context.getSystemService()!!

    fun scheduleAll() {
        NotificationSlot.entries.forEach(::scheduleNext)
    }

    fun scheduleNext(slot: NotificationSlot) {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            slot.nextTriggerMillis(),
            buildPendingIntent(slot, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)!!,
        )
    }

    fun cancelAll() {
        NotificationSlot.entries.forEach { slot ->
            buildPendingIntent(slot, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
                ?.let { alarmManager.cancel(it) }
        }
    }

    private fun buildPendingIntent(slot: NotificationSlot, flags: Int): PendingIntent? {
        val intent = Intent(context, NotificationReceiver::class.java)
            .putExtra(NotificationReceiver.EXTRA_SLOT, slot.name)
        return PendingIntent.getBroadcast(context, slot.requestCode, intent, flags)
    }
}
