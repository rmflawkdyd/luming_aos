package io.github.rmflawkdyd.luming.presentation.home.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompletionOverlayTest {

    @get:Rule val rule = createComposeRule()

    @Test fun `1500ms 후 자동 해제`() {
        var dismissed = false
        rule.setContent {
            CompletionOverlay(onDismiss = { dismissed = true })
        }
        // 합성 직후(1.5초 전)에는 아직 자동 해제되지 않아야 한다.
        rule.waitForIdle()
        assertThat(dismissed).isFalse()

        // 자동 해제는 kotlinx.coroutines.delay(1_500) 기반이라 실제 시간으로 기다린다.
        // mainClock.advanceTimeBy 는 프레임 클럭만 제어해 delay 를 진행시키지 못한다.
        rule.waitUntil(timeoutMillis = 3_000) { dismissed }
        assertThat(dismissed).isTrue()
    }

    @Test fun `탭 시 즉시 해제`() {
        var dismissed = false
        rule.setContent {
            CompletionOverlay(onDismiss = { dismissed = true })
        }
        // 오버레이는 AnimatedVisibility(fadeIn) 안에서 나타나므로, 클릭 전에
        // 합성/애니메이션이 안정될 때까지 기다려야 클릭이 확실히 노드에 도달한다.
        // advanceTimeBy(16) 한 프레임만으로는 표시가 보장되지 않아 flaky 했다.
        rule.waitForIdle()
        rule.onRoot().performClick()
        assertThat(dismissed).isTrue()
    }
}
