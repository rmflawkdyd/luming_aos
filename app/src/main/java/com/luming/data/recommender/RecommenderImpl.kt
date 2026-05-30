package com.luming.data.recommender

import com.luming.domain.model.Activity
import com.luming.domain.model.Category
import com.luming.domain.model.ContextSnapshot
import com.luming.domain.model.ContextTag
import com.luming.domain.model.Recommendation
import com.luming.domain.model.WeatherBucket
import com.luming.domain.recommender.Recommender
import javax.inject.Inject

class RecommenderImpl @Inject constructor(
    private val ruleScorer: RuleScorer,
    private val selector: TFLiteSelector,
) : Recommender {

    override fun recommend(library: List<Activity>, ctx: ContextSnapshot): List<Recommendation> {
        // 1. Hard filter
        val candidates = applyFilters(library, ctx).ifEmpty { library }

        // 2. Rule scoring and sort: score desc, then id asc (deterministic tiebreak)
        val scored = candidates
            .map { it to ruleScorer.score(it, ctx) }
            .sortedWith(compareByDescending<Pair<Activity, Int>> { it.second }.thenBy { it.first.id })

        // 3. Top-K = 3
        val topK = scored.take(3)

        // 4. Rule-relative N-decision scores (always from topK, never from selector output)
        val ruleTop1Score = topK.getOrNull(0)?.second ?: 0
        val ruleTop2Score = topK.getOrNull(1)?.second ?: 0

        // 5. Selector reorders for diversity (display order only)
        val ordered = if (selector.isLoaded && topK.isNotEmpty()) {
            val chosenIdx = selector.pickIndex(ctx, topK)
            val chosen = topK[chosenIdx]
            val remaining = topK
                .filterIndexed { i, _ -> i != chosenIdx }
                .sortedWith(compareByDescending<Pair<Activity, Int>> { it.second }.thenBy { it.first.id })
            listOf(chosen) + remaining
        } else {
            topK
        }

        // 6. N decision using rule-relative scores
        val allZero = topK.all { it.second == 0 }
        val n = when {
            allZero -> ordered.size.coerceAtMost(3)
            ruleTop1Score >= 2 * ruleTop2Score -> 1
            ordered.size >= 3 && (ordered[1].second - ordered[2].second) > ruleScorer.gapThreshold -> 2
            else -> ordered.size.coerceAtMost(3)
        }

        return ordered.take(n).mapIndexed { idx, (activity, _) ->
            Recommendation(
                activity = activity,
                rationale = rationaleFor(activity, ctx),
                rank = idx + 1,
            )
        }
    }

    private fun applyFilters(library: List<Activity>, ctx: ContextSnapshot): List<Activity> {
        val shouldExcludeOutdoor = ctx.isPrecipitating ||
            ctx.weatherBucket == WeatherBucket.HOT ||
            ctx.weatherBucket == WeatherBucket.COLD
        if (!shouldExcludeOutdoor) return library
        return library.filter { activity ->
            !(ContextTag.OUTDOOR in activity.contextTags && ContextTag.INDOOR !in activity.contextTags)
        }
    }

    private fun rationaleFor(activity: Activity, ctx: ContextSnapshot): String {
        val cat = activity.category.displayName()
        return when (ctx.weatherBucket) {
            WeatherBucket.UNKNOWN -> "현재 시간대에 맞는 활동이에요."
            WeatherBucket.CLEAR -> "맑은 날씨에 어울리는 ${cat} 활동이에요."
            WeatherBucket.RAINY -> "비 오는 날엔 실내 ${cat} 활동이 좋아요."
            WeatherBucket.HOT -> "더운 날엔 시원하게 즐기는 ${cat} 활동이에요."
            WeatherBucket.COLD -> "쌀쌀한 날엔 몸을 풀어주는 ${cat} 활동이에요."
            WeatherBucket.CLOUDY -> "흐린 날엔 실내 ${cat} 활동이 좋아요."
        }
    }

    private fun Category.displayName(): String = when (this) {
        Category.BREATHING -> "호흡"
        Category.STRETCH -> "스트레칭"
        Category.MEDITATION -> "명상"
        Category.WALK -> "걷기"
        Category.FOCUS -> "집중"
        Category.MOVEMENT -> "움직임"
        Category.REST -> "휴식"
    }
}
