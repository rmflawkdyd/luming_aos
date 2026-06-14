package com.luming.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.luming.MainActivity
import com.luming.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val slotName = intent.getStringExtra(EXTRA_SLOT) ?: return
        val slot = runCatching { NotificationSlot.valueOf(slotName) }.getOrNull() ?: return
        showNotification(context, slot)
        scheduler.scheduleNext(slot)
    }

    private fun showNotification(context: Context, slot: NotificationSlot) {
        val (titleRes, bodyRes) = when (slot) {
            NotificationSlot.MORNING ->
                R.string.notification_morning_title to R.string.notification_morning_body
            NotificationSlot.AFTERNOON ->
                R.string.notification_afternoon_title to R.string.notification_afternoon_body
            NotificationSlot.EVENING ->
                R.string.notification_evening_title to R.string.notification_evening_body
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            slot.requestCode,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setContentTitle(context.getString(titleRes))
            .setContentText(context.getString(bodyRes))
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        context.getSystemService<NotificationManager>()?.notify(slot.requestCode, notification)
    }

    companion object {
        const val EXTRA_SLOT = "extra_slot"
        const val CHANNEL_ID = "luming_daily"
    }
}
