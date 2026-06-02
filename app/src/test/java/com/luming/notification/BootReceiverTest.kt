package com.luming.notification

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class BootReceiverTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `ACTION_BOOT_COMPLETED 수신 시 슬롯 수만큼 알람 등록됨`() {
        val scheduler = NotificationScheduler(context)
        val receiver = BootReceiver().apply { this.scheduler = scheduler }

        receiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))

        val shadow = shadowOf(context.getSystemService(AlarmManager::class.java))
        assertThat(shadow.scheduledAlarms).hasSize(NotificationSlot.entries.size)
    }

    @Test
    fun `BOOT_COMPLETED 외 다른 action 수신 시 알람 미등록`() {
        val scheduler = NotificationScheduler(context)
        val receiver = BootReceiver().apply { this.scheduler = scheduler }

        receiver.onReceive(context, Intent(Intent.ACTION_POWER_CONNECTED))

        val shadow = shadowOf(context.getSystemService(AlarmManager::class.java))
        assertThat(shadow.scheduledAlarms).isEmpty()
    }
}
