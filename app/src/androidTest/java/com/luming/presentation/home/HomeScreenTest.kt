package com.luming.presentation.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.luming.R
import com.luming.domain.model.Activity
import com.luming.domain.model.Category
import com.luming.domain.model.Recommendation
import com.luming.domain.model.Step
import com.luming.domain.model.Streak
import com.luming.domain.model.TimeBucket
import com.luming.domain.model.WeatherBucket
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun str(id: Int) = context.getString(id)

    private val date = LocalDate(2026, 6, 16)

    private fun streak(count: Int = 3) = Streak(
        currentCount = count,
        lastCompletedDate = null,
        last7Days = List(7) { false },
    )

    private fun activity(id: String, name: String) = Activity(
        id = id,
        name = name,
        category = Category.STRETCH,
        durationMin = 10,
        steps = listOf(Step(order = 1, text = "단계")),
        contextTags = emptySet(),
    )

    private fun recommendation(id: String, name: String, rank: Int) = Recommendation(
        activity = activity(id, name),
        rationale = "이유 $name",
        rank = rank,
    )

    private val sampleRecs = listOf(
        recommendation("a1", "어깨 스트레칭", 1),
        recommendation("a2", "깊은 호흡", 2),
    )

    private fun setHome(
        uiState: HomeUiState,
        locationDenied: Boolean = false,
        onActivityClick: (String) -> Unit = {},
        onRefresh: () -> Unit = {},
        onLocationBannerTap: () -> Unit = {},
        onSettingsClick: () -> Unit = {},
    ) {
        rule.setContent {
            HomeScreen(
                uiState = uiState,
                locationDenied = locationDenied,
                onActivityClick = onActivityClick,
                onRefresh = onRefresh,
                onOverlayDismissed = {},
                onLocationBannerTap = onLocationBannerTap,
                onSettingsClick = onSettingsClick,
            )
        }
        rule.waitForIdle()
    }

    // Empty 상태 — 콘텐츠(앱 이름) 미표시 (스피너만)
    @Test fun `Empty 상태 - 앱 콘텐츠 미표시`() {
        setHome(HomeUiState.Empty)
        rule.onNodeWithText(str(R.string.app_name)).assertDoesNotExist()
        rule.onNodeWithText("어깨 스트레칭").assertDoesNotExist()
    }

    // TimeOnly — 추천 카드들이 렌더링된다
    @Test fun `TimeOnly - 추천 활동 카드 표시`() {
        setHome(HomeUiState.TimeOnly(sampleRecs, streak(), date))
        rule.onNodeWithText("어깨 스트레칭").assertIsDisplayed()
        rule.onNodeWithText("깊은 호흡").assertIsDisplayed()
    }

    // AC-24: locationDenied=false 면 위치 배너가 없다
    @Test fun `위치 허용 - 위치 배너 미표시`() {
        setHome(HomeUiState.TimeOnly(sampleRecs, streak(), date), locationDenied = false)
        rule.onNodeWithText(str(R.string.location_banner_title)).assertDoesNotExist()
    }

    // AC-24: locationDenied=true 면 위치 배너가 상시 노출된다 (비차단)
    @Test fun `위치 거부 - 위치 배너 표시 및 추천도 함께 표시`() {
        setHome(HomeUiState.TimeOnly(sampleRecs, streak(), date), locationDenied = true)
        rule.onNodeWithText(str(R.string.location_banner_title)).assertIsDisplayed()
        // 비차단: 시간 기반 추천은 그대로 보인다
        rule.onNodeWithText("어깨 스트레칭").assertIsDisplayed()
    }

    // AC-24: 위치 배너 탭 → onLocationBannerTap (시스템 설정 딥링크 트리거)
    @Test fun `위치 배너 탭 - onLocationBannerTap 호출`() {
        var tapped = false
        setHome(
            HomeUiState.TimeOnly(sampleRecs, streak(), date),
            locationDenied = true,
            onLocationBannerTap = { tapped = true },
        )
        rule.onNodeWithContentDescription(
            "${str(R.string.location_banner_title)}. ${str(R.string.location_banner_subtitle)}",
        ).performClick()
        assertThat(tapped).isTrue()
    }

    // AC-25: 기어 탭 → onSettingsClick (SettingsView 진입점)
    @Test fun `설정 기어 탭 - onSettingsClick 호출`() {
        var clicked = false
        setHome(HomeUiState.TimeOnly(sampleRecs, streak(), date), onSettingsClick = { clicked = true })
        rule.onNodeWithContentDescription(str(R.string.cd_settings)).performClick()
        assertThat(clicked).isTrue()
    }

    // 활동 카드 탭 → onActivityClick(해당 id)
    @Test fun `활동 카드 탭 - 해당 activity id로 onActivityClick 호출`() {
        var clickedId: String? = null
        setHome(HomeUiState.TimeOnly(sampleRecs, streak(), date), onActivityClick = { clickedId = it })
        rule.onNodeWithText("어깨 스트레칭").performClick()
        assertThat(clickedId).isEqualTo("a1")
    }

    // WeatherAware — 날씨 근거 배너 + 추천 카드 표시
    @Test fun `WeatherAware - 날씨 근거 문구 표시`() {
        setHome(HomeUiState.WeatherAware(sampleRecs, streak(), date, WeatherBucket.RAINY))
        rule.onNodeWithText(str(R.string.rationale_rainy)).assertIsDisplayed()
        rule.onNodeWithText("어깨 스트레칭").assertIsDisplayed()
    }

    // WeatherFailed — 에러 + 다시 시도 버튼, 탭 시 onRefresh
    @Test fun `WeatherFailed - 에러 메시지 표시 및 재시도 콜백`() {
        var refreshed = false
        setHome(HomeUiState.WeatherFailed, onRefresh = { refreshed = true })
        rule.onNodeWithText(str(R.string.error_weather)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.action_retry)).performClick()
        assertThat(refreshed).isTrue()
    }

    // CompletedSlot — 시간대 완료 안내 표시
    @Test fun `CompletedSlot MORNING - 완료 안내 표시`() {
        setHome(HomeUiState.CompletedSlot(TimeBucket.MORNING, streak(), date))
        // TimeSlotCompletedContent 는 clearAndSetSemantics 로 자식 텍스트를 지우고
        // 단일 contentDescription("완료 라벨. 다음 안내")으로 대체하므로 cd 로 단언한다.
        rule.onNodeWithContentDescription(str(R.string.slot_completed_morning), substring = true).assertIsDisplayed()
    }

    // RestPrompt — 휴식 안내 문구 표시 (00~04시), 추천 카드 없음
    @Test fun `RestPrompt - 휴식 안내 표시 및 추천 카드 없음`() {
        setHome(HomeUiState.RestPrompt(streak(), date))
        rule.onNodeWithText(str(R.string.rest_prompt_title)).assertIsDisplayed()
        rule.onNodeWithText("어깨 스트레칭").assertDoesNotExist()
    }
}
