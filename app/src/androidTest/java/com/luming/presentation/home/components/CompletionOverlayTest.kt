package com.luming.presentation.home.components

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
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompletionOverlay(onDismiss = { dismissed = true })
        }
        rule.waitForIdle()

        rule.mainClock.advanceTimeBy(1_499)
        assertThat(dismissed).isFalse()

        rule.mainClock.advanceTimeBy(1)
        rule.waitForIdle()
        assertThat(dismissed).isTrue()
    }

    @Test fun `탭 시 즉시 해제`() {
        var dismissed = false
        rule.setContent {
            CompletionOverlay(onDismiss = { dismissed = true })
        }
        rule.mainClock.advanceTimeBy(16)
        rule.onRoot().performClick()
        assertThat(dismissed).isTrue()
    }
}
