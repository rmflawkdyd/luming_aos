package com.luming.presentation.instruction

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.luming.R
import com.luming.domain.model.Activity
import com.luming.domain.model.Category
import com.luming.domain.model.Step
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InstructionScreenTest {

    @get:Rule val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun str(id: Int) = context.getString(id)

    private val testActivity = Activity(
        id = "test",
        name = "Test Activity",
        category = Category.STRETCH,
        durationMin = 10,
        steps = listOf(Step(order = 1, text = "첫 번째 단계")),
        contextTags = emptySet(),
    )

    // AC-T1 / AC-T7: 진입 시 시작버튼 표시, 완료버튼 숨김
    @Test fun `타이머 미시작 - 시작버튼 표시`() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            InstructionScreen(
                uiState = InstructionUiState(testActivity),
                onNext = {}, onPrevious = {}, onStart = {}, onComplete = {},
                onConfirmWarning = {}, onDismissWarning = {}, onBack = {},
                onConfirmAbort = {}, onDismissAbort = {},
            )
        }
        rule.waitForIdle()
        rule.onNodeWithText(str(R.string.action_start)).assertIsDisplayed()
    }

    // AC-T1 / AC-T7: 시작 전 완료버튼 없음
    @Test fun `타이머 미시작 - 완료버튼 없음`() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            InstructionScreen(
                uiState = InstructionUiState(testActivity),
                onNext = {}, onPrevious = {}, onStart = {}, onComplete = {},
                onConfirmWarning = {}, onDismissWarning = {}, onBack = {},
                onConfirmAbort = {}, onDismissAbort = {},
            )
        }
        rule.waitForIdle()
        // 완료 버튼은 isTimerRunning=false이므로 존재하지 않아야 함
        rule.onNodeWithText(str(R.string.action_complete)).assertDoesNotExist()
    }

    // AC-T2: 타이머 실행 중 완료버튼 표시, 시작버튼 사라짐
    @Test fun `타이머 실행 중 - 완료버튼 표시`() {
        rule.mainClock.autoAdvance = false
        val state = InstructionUiState(testActivity, timerStartedAt = System.currentTimeMillis())
        rule.setContent {
            InstructionScreen(
                uiState = state,
                onNext = {}, onPrevious = {}, onStart = {}, onComplete = {},
                onConfirmWarning = {}, onDismissWarning = {}, onBack = {},
                onConfirmAbort = {}, onDismissAbort = {},
            )
        }
        rule.mainClock.advanceTimeBy(16)
        rule.waitForIdle()
        rule.onNodeWithText(str(R.string.action_complete)).assertIsDisplayed()
    }

    // AC-T2: 타이머 실행 중 시작버튼 없음
    @Test fun `타이머 실행 중 - 시작버튼 없음`() {
        rule.mainClock.autoAdvance = false
        val state = InstructionUiState(testActivity, timerStartedAt = System.currentTimeMillis())
        rule.setContent {
            InstructionScreen(
                uiState = state,
                onNext = {}, onPrevious = {}, onStart = {}, onComplete = {},
                onConfirmWarning = {}, onDismissWarning = {}, onBack = {},
                onConfirmAbort = {}, onDismissAbort = {},
            )
        }
        rule.mainClock.advanceTimeBy(16)
        rule.waitForIdle()
        rule.onNodeWithText(str(R.string.action_start)).assertDoesNotExist()
    }

    // AC-T4: showTimerWarning=true → 조기 완료 다이얼로그 표시
    @Test fun `showTimerWarning true - 조기 완료 다이얼로그 표시`() {
        val state = InstructionUiState(
            testActivity,
            timerStartedAt = System.currentTimeMillis(),
            showTimerWarning = true,
        )
        rule.setContent {
            InstructionScreen(
                uiState = state,
                onNext = {}, onPrevious = {}, onStart = {}, onComplete = {},
                onConfirmWarning = {}, onDismissWarning = {}, onBack = {},
                onConfirmAbort = {}, onDismissAbort = {},
            )
        }
        rule.waitForIdle()
        rule.onNodeWithText(str(R.string.dialog_early_complete_title)).assertIsDisplayed()
    }

    // 스펙 §2.2 aborting: showAbortWarning=true → 이탈 다이얼로그 표시
    @Test fun `showAbortWarning true - 이탈 다이얼로그 표시`() {
        val state = InstructionUiState(
            testActivity,
            timerStartedAt = System.currentTimeMillis(),
            showAbortWarning = true,
        )
        rule.setContent {
            InstructionScreen(
                uiState = state,
                onNext = {}, onPrevious = {}, onStart = {}, onComplete = {},
                onConfirmWarning = {}, onDismissWarning = {}, onBack = {},
                onConfirmAbort = {}, onDismissAbort = {},
            )
        }
        rule.waitForIdle()
        rule.onNodeWithText(str(R.string.dialog_abort_title)).assertIsDisplayed()
    }

    // 그만두기 버튼 클릭 → onConfirmAbort 호출
    @Test fun `이탈 다이얼로그 - 그만두기 클릭 시 onConfirmAbort 호출`() {
        var confirmed = false
        val state = InstructionUiState(
            testActivity,
            timerStartedAt = System.currentTimeMillis(),
            showAbortWarning = true,
        )
        rule.setContent {
            InstructionScreen(
                uiState = state,
                onNext = {}, onPrevious = {}, onStart = {}, onComplete = {},
                onConfirmWarning = {}, onDismissWarning = {}, onBack = {},
                onConfirmAbort = { confirmed = true }, onDismissAbort = {},
            )
        }
        rule.waitForIdle()
        rule.onNodeWithText(str(R.string.action_abort)).performClick()
        assertThat(confirmed).isTrue()
    }

    // 계속하기 버튼 클릭 → onDismissAbort 호출
    @Test fun `이탈 다이얼로그 - 계속하기 클릭 시 onDismissAbort 호출`() {
        var dismissed = false
        val state = InstructionUiState(
            testActivity,
            timerStartedAt = System.currentTimeMillis(),
            showAbortWarning = true,
        )
        rule.setContent {
            InstructionScreen(
                uiState = state,
                onNext = {}, onPrevious = {}, onStart = {}, onComplete = {},
                onConfirmWarning = {}, onDismissWarning = {}, onBack = {},
                onConfirmAbort = {}, onDismissAbort = { dismissed = true },
            )
        }
        rule.waitForIdle()
        rule.onNodeWithText(str(R.string.action_continue)).performClick()
        assertThat(dismissed).isTrue()
    }

    // 타이머 미시작 시 이탈 다이얼로그 미표시
    @Test fun `showAbortWarning false - 이탈 다이얼로그 미표시`() {
        rule.setContent {
            InstructionScreen(
                uiState = InstructionUiState(testActivity),
                onNext = {}, onPrevious = {}, onStart = {}, onComplete = {},
                onConfirmWarning = {}, onDismissWarning = {}, onBack = {},
                onConfirmAbort = {}, onDismissAbort = {},
            )
        }
        rule.waitForIdle()
        rule.onNodeWithText(str(R.string.dialog_abort_title)).assertDoesNotExist()
    }

    // AC-T9: 경과 시간 >= 목표 시 onComplete 자동 호출, elapsed == targetMs
    @Test fun `경과 시간이 목표 초과 시 onComplete 자동 호출`() {
        val targetMs = testActivity.durationMin * 60_000L
        val startedAt = System.currentTimeMillis() - targetMs - 5_000L
        var capturedElapsed = -1L
        rule.setContent {
            InstructionScreen(
                uiState = InstructionUiState(testActivity, timerStartedAt = startedAt),
                onNext = {}, onPrevious = {}, onStart = {},
                onComplete = { capturedElapsed = it },
                onConfirmWarning = {}, onDismissWarning = {}, onBack = {},
                onConfirmAbort = {}, onDismissAbort = {},
            )
        }
        rule.waitForIdle()
        assertThat(capturedElapsed).isEqualTo(targetMs)
    }
}
