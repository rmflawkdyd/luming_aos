package io.github.rmflawkdyd.luming.data.recommender

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.github.rmflawkdyd.luming.domain.model.Activity
import io.github.rmflawkdyd.luming.domain.model.Category
import io.github.rmflawkdyd.luming.domain.model.ContextSnapshot
import io.github.rmflawkdyd.luming.domain.model.ContextTag
import io.github.rmflawkdyd.luming.domain.model.TimeBucket
import io.github.rmflawkdyd.luming.domain.model.WeatherBucket
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], application = android.app.Application::class)
class RuleScorerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val scorer = RuleScorer(context)

    private val morningClearActivity = Activity(
        id = "test_morning",
        name = "Morning Stretch",
        category = Category.STRETCH,
        durationMin = 10,
        steps = emptyList(),
        contextTags = setOf(ContextTag.MORNING, ContextTag.CLEAR, ContextTag.OUTDOOR),
    )

    private val morningClearCtx = ContextSnapshot(
        timeBucket = TimeBucket.MORNING,
        weatherBucket = WeatherBucket.CLEAR,
        dayOfWeekHash = 0,
        isPrecipitating = false,
    )

    @Test fun `동일 입력 10회 반복 - 항상 같은 점수`() {
        val scores = (1..10).map { scorer.score(morningClearActivity, morningClearCtx) }
        assertThat(scores.toSet()).hasSize(1)
    }

    @Test fun `MORNING 태그 활동 - MORNING 컨텍스트에서 점수 0 초과`() {
        val score = scorer.score(morningClearActivity, morningClearCtx)
        assertThat(score).isGreaterThan(0)
    }

    @Test fun `태그 불일치 활동 - 점수 0`() {
        val nightRainyActivity = Activity(
            id = "test_night",
            name = "Night Rest",
            category = Category.REST,
            durationMin = 10,
            steps = emptyList(),
            contextTags = setOf(ContextTag.NIGHT, ContextTag.RAINY, ContextTag.INDOOR),
        )
        val score = scorer.score(nightRainyActivity, morningClearCtx)
        assertThat(score).isEqualTo(0)
    }

    @Test fun `MORNING보다 AFTERNOON 컨텍스트에서 MORNING 태그 활동 점수 같거나 낮음`() {
        val afternoonCtx = morningClearCtx.copy(timeBucket = TimeBucket.AFTERNOON)
        val morningScore = scorer.score(morningClearActivity, morningClearCtx)
        val afternoonScore = scorer.score(morningClearActivity, afternoonCtx)
        assertThat(morningScore).isAtLeast(afternoonScore)
    }

    @Test fun `RAINY 컨텍스트에서 INDOOR 태그 활동 - 점수 0 이상`() {
        val indoorRainyActivity = Activity(
            id = "test_indoor",
            name = "Indoor Meditation",
            category = Category.MEDITATION,
            durationMin = 10,
            steps = emptyList(),
            contextTags = setOf(ContextTag.RAINY, ContextTag.INDOOR),
        )
        val rainyCtx = morningClearCtx.copy(weatherBucket = WeatherBucket.RAINY)
        val score = scorer.score(indoorRainyActivity, rainyCtx)
        assertThat(score).isAtLeast(0)
    }
}
