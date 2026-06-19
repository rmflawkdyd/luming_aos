package io.github.rmflawkdyd.luming

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.content.getSystemService
import io.github.rmflawkdyd.luming.notification.NotificationReceiver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LumingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NotificationReceiver.CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString(R.string.notification_channel_desc)
        }
        getSystemService<NotificationManager>()?.createNotificationChannel(channel)
    }
}
