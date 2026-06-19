package io.github.rmflawkdyd.luming.data.recommender

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import io.github.rmflawkdyd.luming.data.activity.local.dto.ActivityLibraryDto
import io.github.rmflawkdyd.luming.domain.model.Activity
import io.github.rmflawkdyd.luming.domain.model.ContextSnapshot
import io.github.rmflawkdyd.luming.domain.model.TimeBucket
import io.github.rmflawkdyd.luming.domain.model.WeatherBucket
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * 카테고리 다양성(spec §6.3 / ADR-014) 검증.
 *
 * RuleScorer + pickCategoryDiverse Top-K가 4 TimeBucket × 6 WeatherBucket = 24개 맥락
 * 전부에서 단일 카테고리에 독점되지 않는지 확인한다. 셀렉터는 표시 순서만 바꾸므로
 * (분포 불변) null interpreter로 고정해 결과를 결정적으로 만든다.
 *
 * 골든 기준값(shared rule_scorer.py): 24개 맥락 모두 양수 점수 후보 3개 = 3개 distinct 카테고리.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], application = android.app.Application::class)
class RecommenderImplTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val context: Context = ApplicationProvider.getApplicationContext()

    private val recommender = RecommenderImpl(
        ruleScorer = RuleScorer(context),
        // null interpreter → isLoaded=false → 셀렉터 재정렬 없음(결정적). 분포는 동일.
        selector = TFLiteSelector(interpreter = null, activityIndexTable = emptyMap()),
        context = context,
    )

    private val library: List<Activity> by lazy {
        val text = context.assets.open("activities.v1.json").bufferedReader().readText()
        json.decodeFromString<ActivityLibraryDto>(text).activities
    }

    /**
     * 24개 맥락 전수: 각 맥락의 추천 리스트가 (1) 비어있지 않고 (2) 카테고리가 모두 서로 달라
     * 단일 카테고리에 독점되지 않는다. 70개 활동 라이브러리는 7개 카테고리를 모두 포함하므로
     * 모든 맥락에서 정확히 3개 distinct 카테고리가 나와야 한다.
     */
    @Test fun `24개 맥락 - 추천 카테고리 모두 distinct(독점 없음)`() {
        for (time in TimeBucket.entries) {
            for (weather in WeatherBucket.entries) {
                val ctx = ContextSnapshot(
                    timeBucket = time,
                    weatherBucket = weather,
                    dayOfWeekHash = 0,
                    // golden 의미론과 일치: rainy 는 강수로 취급(야외 필터 적용)
                    isPrecipitating = weather == WeatherBucket.RAINY,
                )

                val recs = recommender.recommend(library, ctx)
                val categories = recs.map { it.activity.category }
                val label = "context=${time}_${weather}"

                assertWithMessage("$label — 추천이 비어있음").that(recs).isNotEmpty()

                // 핵심 불변식: 카테고리 중복 없음 (set 크기 == 리스트 크기)
                assertWithMessage("$label — 카테고리 중복 발생: $categories")
                    .that(categories.toSet().size)
                    .isEqualTo(categories.size)

                // 풀 라이브러리(7개 카테고리)에서는 정확히 3개 distinct 카테고리
                assertWithMessage("$label — distinct 카테고리 수: $categories")
                    .that(categories.toSet().size)
                    .isEqualTo(3)
            }
        }
    }

    /** rank 는 1부터 연속이어야 한다 (표시 순서 계약). */
    @Test fun `24개 맥락 - rank 1부터 연속`() {
        for (time in TimeBucket.entries) {
            for (weather in WeatherBucket.entries) {
                val ctx = ContextSnapshot(
                    timeBucket = time,
                    weatherBucket = weather,
                    dayOfWeekHash = 0,
                    isPrecipitating = weather == WeatherBucket.RAINY,
                )
                val recs = recommender.recommend(library, ctx)
                assertWithMessage("context=${time}_${weather} — rank 불연속: ${recs.map { it.rank }}")
                    .that(recs.map { it.rank })
                    .isEqualTo((1..recs.size).toList())
            }
        }
    }
}
