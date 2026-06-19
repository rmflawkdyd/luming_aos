package io.github.rmflawkdyd.luming.presentation.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * 시스템 앱 설정으로 이동하는 outbound 딥링크 헬퍼 (spec §8.5, ADR-012).
 *
 * `ACTION_APPLICATION_DETAILS_SETTINGS`는 **outbound** 내비게이션이며,
 * 앱에 URL 스킴이나 inbound 딥링크(intent-filter)를 추가하지 않는다 (AC-20/AC-27 불위반).
 * 위치/알림 권한은 프로그램적으로 변경할 수 없으므로, 재유도는 항상 이 화면을 연다.
 */
object SystemSettings {
    fun open(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}
