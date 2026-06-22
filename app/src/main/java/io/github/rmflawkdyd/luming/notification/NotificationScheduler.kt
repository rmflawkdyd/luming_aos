package io.github.rmflawkdyd.luming.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import io.github.rmflawkdyd.luming.domain.util.Clock
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fireStore: NotificationFireStore,
    private val clock: Clock,
) {
    private val alarmManager: AlarmManager = context.getSystemService()!!

    /** 알림이 OS 차원에서 활성화돼 있는지 — SettingsScreen 권한 행 상태 표시용 (iOS isAuthorized 패리티). */
    fun isAuthorized(): Boolean {
        val manager = context.getSystemService<NotificationManager>() ?: return false
        return manager.areNotificationsEnabled()
    }

    suspend fun scheduleAll() {
        NotificationSlot.entries.forEach { scheduleNext(it) }
    }

    suspend fun scheduleNext(slot: NotificationSlot) {
        val firedToday = fireStore.lastFired(slot) == clock.today()
        val triggerAt = slot.computeTriggerMillis(
            now = System.currentTimeMillis(),
            alreadyFiredToday = firedToday,
        )
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
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
