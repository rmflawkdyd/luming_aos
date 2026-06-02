package com.luming.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.luming.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    val needsPermission: Boolean = !hasRequiredPermissions()

    private val _permissionsHandled = MutableStateFlow(!needsPermission)
    val permissionsHandled: StateFlow<Boolean> = _permissionsHandled.asStateFlow()

    init {
        if (!needsPermission) {
            notificationScheduler.scheduleAll()
        } else {
            _permissionsHandled.value = true
        }
    }

    fun onPermissionsResult(notificationGranted: Boolean) {
        if (notificationGranted) notificationScheduler.scheduleAll()
        _permissionsHandled.value = true
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
