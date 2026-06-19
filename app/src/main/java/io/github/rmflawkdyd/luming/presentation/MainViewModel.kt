package io.github.rmflawkdyd.luming.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import io.github.rmflawkdyd.luming.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    val needsPermission: Boolean = !hasRequiredPermissions()

    init {
        if (!needsPermission) notificationScheduler.scheduleAll()
    }

    fun onPermissionsResult(notificationGranted: Boolean) {
        if (notificationGranted) notificationScheduler.scheduleAll()
    }

    private fun hasRequiredPermissions(): Boolean {
        val locationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        return locationGranted && notifGranted
    }
}
