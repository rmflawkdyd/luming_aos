package com.luming.data.recommender

import android.content.Context
import com.luming.R
import com.luming.domain.model.Activity
import com.luming.domain.model.Category
import com.luming.domain.model.ContextSnapshot
import com.luming.domain.model.ContextTag
import com.luming.domain.model.Recommendation
import com.luming.domain.model.WeatherBucket
import com.luming.domain.recommender.Recommender
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RecommenderImpl @Inject constructor(
    private val ruleScorer: RuleScorer,
    private val selector: TFLiteSelector,
    @ApplicationContext private val context: Context,
) : Recommender {

    override fun recommend(library: List<Activity>, ctx: ContextSnapshot): List<Recommendation> {
        // 1. Hard filter — fallback to full library when all candidates are filtered out
        val filtered = applyFilters(library, ctx)
        val isFallback = filtered.isEmpty()
        val candidates = filtered.ifEmpty { library }

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

        // Fallback rationale treats weather as unknown (spec §6.2)
        val rationaleCtx = if (isFallback) ctx.copy(weatherBucket = WeatherBucket.UNKNOWN) else ctx
        return ordered.take(n).mapIndexed { idx, (activity, _) ->
            Recommendation(
                activity = activity,
                rationale = rationaleFor(activity, rationaleCtx),
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
            WeatherBucket.UNKNOWN -> context.getString(R.string.recommendation_rationale_unknown)
            WeatherBucket.CLEAR -> context.getString(R.string.recommendation_rationale_clear, cat)
            WeatherBucket.RAINY -> context.getString(R.string.recommendation_rationale_rainy, cat)
            WeatherBucket.HOT -> context.getString(R.string.recommendation_rationale_hot, cat)
            WeatherBucket.COLD -> context.getString(R.string.recommendation_rationale_cold, cat)
            WeatherBucket.CLOUDY -> context.getString(R.string.recommendation_rationale_cloudy, cat)
        }
    }

    private fun Category.displayName(): String = when (this) {
        Category.BREATHING -> context.getString(R.string.category_breathing)
        Category.STRETCH -> context.getString(R.string.category_stretch)
        Category.MEDITATION -> context.getString(R.string.category_meditation)
        Category.WALK -> context.getString(R.string.category_walk)
        Category.FOCUS -> context.getString(R.string.category_focus)
        Category.MOVEMENT -> context.getString(R.string.category_movement)
        Category.REST -> context.getString(R.string.category_rest)
    }
}
