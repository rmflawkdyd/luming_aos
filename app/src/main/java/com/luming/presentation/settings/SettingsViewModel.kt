package com.luming.presentation.settings

import android.content.Context
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import com.luming.notification.NotificationScheduler
import com.luming.domain.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * SettingsScreen 상태 — 위치/알림 권한의 라이브 상태를 OS에서 읽어 표시 (§2.5, AC-25/AC-26).
 * iOS `SettingsViewModel` 패리티. OS 권한은 프로그램적으로 바꿀 수 없으므로 토글은
 * 상태 반영용 시각 요소이며, 행을 탭하면 시스템 설정 딥링크로 이동한다(§8.5).
 * 설정에서 돌아오면(ON_RESUME) refresh()로 상태를 다시 읽는다.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationRepository: LocationRepository,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    private val _locationGranted = MutableStateFlow(false)
    val locationGranted: StateFlow<Boolean> = _locationGranted.asStateFlow()

    private val _notificationGranted = MutableStateFlow(false)
    val notificationGranted: StateFlow<Boolean> = _notificationGranted.asStateFlow()

    val appVersion: String = readAppVersion()

    init {
        refresh()
    }

    fun refresh() {
        _locationGranted.value = locationRepository.hasPermission()
        _notificationGranted.value = notificationScheduler.isAuthorized()
    }

    private fun readAppVersion(): String {
        return runCatching {
            val info: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${info.versionName}"
        }.getOrDefault("1.0")
    }
}
