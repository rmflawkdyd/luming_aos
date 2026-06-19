package io.github.rmflawkdyd.luming.data.recommender

import android.content.Context
import io.github.rmflawkdyd.luming.R
import io.github.rmflawkdyd.luming.domain.model.Activity
import io.github.rmflawkdyd.luming.domain.model.Category
import io.github.rmflawkdyd.luming.domain.model.ContextSnapshot
import io.github.rmflawkdyd.luming.domain.model.ContextTag
import io.github.rmflawkdyd.luming.domain.model.Recommendation
import io.github.rmflawkdyd.luming.domain.model.WeatherBucket
import io.github.rmflawkdyd.luming.domain.recommender.Recommender
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

        // 3. Category-diverse Top-K = 3 (spec §6.3) — prevents a single category from
        //    monopolising the list (all-walk in clear weather / all-breathing on score ties).
        val topK = pickCategoryDiverse(scored, k = 3)
        if (topK.isEmpty()) return emptyList()

        // 4. Diversity-first N-decision: show every category-diverse candidate with a
        //    positive contextual score (1..3). When all score 0 (time-only, no tag match),
        //    show up to 3. N derives from rule scores only — never from selector order.
        val allZero = topK.all { it.second == 0 }
        val displaySet = if (allZero) topK else topK.filter { it.second > 0 }

        // 5. Selector reorders for cross-day featured rotation (display order only)
        val ordered = if (selector.isLoaded && displaySet.isNotEmpty()) {
            val chosenIdx = selector.pickIndex(ctx, displaySet)
            val chosen = displaySet[chosenIdx]
            val remaining = displaySet
                .filterIndexed { i, _ -> i != chosenIdx }
                .sortedWith(compareByDescending<Pair<Activity, Int>> { it.second }.thenBy { it.first.id })
            listOf(chosen) + remaining
        } else {
            displaySet
        }

        // Fallback rationale treats weather as unknown (spec §6.2)
        val rationaleCtx = if (isFallback) ctx.copy(weatherBucket = WeatherBucket.UNKNOWN) else ctx
        return ordered.mapIndexed { idx, (activity, _) ->
            Recommendation(
                activity = activity,
                rationale = rationaleFor(activity, rationaleCtx),
                rank = idx + 1,
            )
        }
    }

    /**
     * Diversity-first Top-K selection (spec §6.3).
     * Pass A picks the highest-scoring representative of each distinct category in score
     * order; Pass B backfills if fewer than [k] distinct categories exist. The result is
     * re-sorted by (score desc, id asc) for a stable top-K order.
     */
    private fun pickCategoryDiverse(
        scored: List<Pair<Activity, Int>>,
        k: Int,
    ): List<Pair<Activity, Int>> {
        val picked = ArrayList<Pair<Activity, Int>>(k)
        val seenCategories = HashSet<Category>()
        for (pair in scored) {
            if (pair.first.category !in seenCategories) {
                picked.add(pair)
                seenCategories.add(pair.first.category)
            }
            if (picked.size == k) break
        }
        if (picked.size < k) {
            val chosenIds = picked.mapTo(HashSet()) { it.first.id }
            for (pair in scored) {
                if (pair.first.id !in chosenIds) {
                    picked.add(pair)
                    if (picked.size == k) break
                }
            }
        }
        return picked.sortedWith(
            compareByDescending<Pair<Activity, Int>> { it.second }.thenBy { it.first.id },
        )
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
