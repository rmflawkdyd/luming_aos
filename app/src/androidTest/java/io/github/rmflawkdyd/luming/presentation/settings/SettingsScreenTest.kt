package io.github.rmflawkdyd.luming.presentation.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.github.rmflawkdyd.luming.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun str(id: Int) = context.getString(id)

    private fun setSettings(
        locationGranted: Boolean = true,
        notificationGranted: Boolean = true,
        appVersion: String = "1.0",
        onBack: () -> Unit = {},
        onPermissionRowTap: () -> Unit = {},
    ) {
        rule.setContent {
            SettingsScreen(
                locationGranted = locationGranted,
                notificationGranted = notificationGranted,
                appVersion = appVersion,
                onBack = onBack,
                onPermissionRowTap = onPermissionRowTap,
            )
        }
        rule.waitForIdle()
    }

    // AC-25: 권한 행(위치/알림)과 제목이 표시된다
    @Test fun `권한 행과 제목 표시`() {
        setSettings()
        rule.onNodeWithText(str(R.string.settings_title)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.settings_row_location)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.settings_row_notification)).assertIsDisplayed()
    }

    // AC-26: 버전 정보가 전달된 값으로 표시된다
    @Test fun `앱 버전 표시`() {
        setSettings(appVersion = "9.9.9")
        rule.onNodeWithText("9.9.9").assertIsDisplayed()
    }

    // 위치 허용/알림 거부 상태가 각 행 접근성 라벨에 반영된다
    @Test fun `권한 상태 - 허용됨_거부됨이 접근성 라벨에 반영`() {
        setSettings(locationGranted = true, notificationGranted = false)
        rule.onNodeWithContentDescription(
            "${str(R.string.settings_row_location)}, ${str(R.string.settings_permission_granted)}",
            substring = true,
        ).assertExists()
        rule.onNodeWithContentDescription(
            "${str(R.string.settings_row_notification)}, ${str(R.string.settings_permission_denied)}",
            substring = true,
        ).assertExists()
    }

    // AC-26: 위치 권한 행 탭 → onPermissionRowTap (시스템 설정 딥링크)
    @Test fun `위치 행 탭 - onPermissionRowTap 호출`() {
        var tapped = false
        setSettings(onPermissionRowTap = { tapped = true })
        rule.onNodeWithContentDescription(str(R.string.settings_row_location), substring = true).performClick()
        assertThat(tapped).isTrue()
    }

    // AC-26: 알림 권한 행 탭 → onPermissionRowTap
    @Test fun `알림 행 탭 - onPermissionRowTap 호출`() {
        var tapped = false
        setSettings(onPermissionRowTap = { tapped = true })
        rule.onNodeWithContentDescription(str(R.string.settings_row_notification), substring = true).performClick()
        assertThat(tapped).isTrue()
    }

    // 뒤로 버튼 탭 → onBack
    @Test fun `뒤로 버튼 탭 - onBack 호출`() {
        var backed = false
        setSettings(onBack = { backed = true })
        rule.onNodeWithContentDescription(str(R.string.cd_back)).performClick()
        assertThat(backed).isTrue()
    }
}
