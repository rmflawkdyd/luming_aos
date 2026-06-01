package com.luming.data.recommender

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertWithMessage
import com.luming.data.activity.local.dto.ActivityLibraryDto
import com.luming.domain.model.Activity
import com.luming.domain.model.ContextSnapshot
import com.luming.domain.model.TimeBucket
import com.luming.domain.model.WeatherBucket
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * AC-5: 동일 입력에 대해 Android RuleScorer의 top-3가 Python rule_scorer.py 결과(golden)와 일치하는지 검증.
 * golden_recommendations.json은 shared/tests/golden_recommendations.json 복사본.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class GoldenRecommendationsTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val scorer = RuleScorer(context)

    private val activities: List<Activity> by lazy {
        val text = context.assets.open("activities.v1.json").bufferedReader().readText()
        json.decodeFromString<ActivityLibraryDto>(text).activities
    }

    private val golden: GoldenDto by lazy {
        val text = javaClass.classLoader!!
            .getResourceAsStream("golden_recommendations.json")!!
            .bufferedReader().readText()
        json.decodeFromString(text)
    }

    /**
     * 각 golden 활동에 대해 Android 스코어러가 동일한 점수를 계산하는지 검증.
     * top-3 ID 순서는 비교하지 않음 — 동점 타이브레이크가 Python(삽입 순서)과
     * Kotlin(알파벳 ID) 사이에 다를 수 있기 때문.
     */
    @Test fun `golden 활동별 룰 스코어가 일치`() {
        golden.entries.forEach { entry ->
            val ctx = entry.context.toSnapshot()
            entry.recommendations.forEach { rec ->
                val activity = activities.first { it.id == rec.activityId }
                val score = scorer.score(activity, ctx)
                assertWithMessage("context=${entry.context.id}, activity=${rec.activityId}")
                    .that(score)
                    .isEqualTo(rec.ruleScore)
            }
        }
    }


    private fun GoldenContextDto.toSnapshot() = ContextSnapshot(
        timeBucket = when (timeBucket) {
            "morning" -> TimeBucket.MORNING
            "afternoon" -> TimeBucket.AFTERNOON
            "evening" -> TimeBucket.EVENING
            "night" -> TimeBucket.NIGHT
            else -> TimeBucket.MORNING
        },
        weatherBucket = when (weatherBucket) {
            "clear" -> WeatherBucket.CLEAR
            "cloudy" -> WeatherBucket.CLOUDY
            "rainy" -> WeatherBucket.RAINY
            "hot" -> WeatherBucket.HOT
            "cold" -> WeatherBucket.COLD
            else -> WeatherBucket.UNKNOWN
        },
        dayOfWeekHash = dayOfWeekHash,
        isPrecipitating = isPrecipitating,
    )

    @Serializable
    data class GoldenDto(val version: String, val entries: List<GoldenEntryDto>)

    @Serializable
    data class GoldenEntryDto(
        val context: GoldenContextDto,
        val recommendations: List<GoldenRecommendationDto>,
    )

    @Serializable
    data class GoldenContextDto(
        val id: String,
        @SerialName("time_bucket") val timeBucket: String,
        @SerialName("weather_bucket") val weatherBucket: String,
        @SerialName("day_of_week_hash") val dayOfWeekHash: Int,
        @SerialName("is_precipitating") val isPrecipitating: Boolean,
    )

    @Serializable
    data class GoldenRecommendationDto(
        val rank: Int,
        @SerialName("activity_id") val activityId: String,
        @SerialName("rule_score") val ruleScore: Int,
    )
}
