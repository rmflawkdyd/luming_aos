package com.luming.data.recommender

import android.content.Context
import com.luming.domain.model.Activity
import com.luming.domain.model.ContextSnapshot
import com.luming.domain.model.ContextTag
import com.luming.domain.model.TimeBucket
import com.luming.domain.model.WeatherBucket
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleScorer @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val rules: ScoringRulesDto by lazy { loadRules() }

    val gapThreshold: Int get() = rules.gapThreshold

    fun score(activity: Activity, ctx: ContextSnapshot): Int {
        var score = 0
        val weights = rules.tagWeights

        // Time bucket contribution
        val timeTag = ctx.timeBucket.toContextTag()
        if (timeTag in activity.contextTags) {
            score += weights[timeTag.jsonKey] ?: 0
        }

        // Weather bucket contribution
        when (ctx.weatherBucket) {
            WeatherBucket.CLEAR -> {
                if (ContextTag.CLEAR in activity.contextTags) score += weights["clear"] ?: 0
                if (ContextTag.OUTDOOR in activity.contextTags) score += weights["outdoor"] ?: 0
            }
            WeatherBucket.CLOUDY -> {
                if (ContextTag.CLOUDY in activity.contextTags) score += weights["cloudy"] ?: 0
            }
            WeatherBucket.RAINY -> {
                if (ContextTag.RAINY in activity.contextTags) score += weights["rainy"] ?: 0
                if (ContextTag.INDOOR in activity.contextTags) score += weights["indoor"] ?: 0
            }
            WeatherBucket.HOT -> {
                if (ContextTag.HOT in activity.contextTags) score += weights["hot"] ?: 0
                if (ContextTag.INDOOR in activity.contextTags) score += weights["indoor"] ?: 0
            }
            WeatherBucket.COLD -> {
                if (ContextTag.COLD in activity.contextTags) score += weights["cold"] ?: 0
                if (ContextTag.INDOOR in activity.contextTags) score += weights["indoor"] ?: 0
            }
            WeatherBucket.UNKNOWN -> { /* no weather contribution */ }
        }

        return score
    }

    private fun TimeBucket.toContextTag(): ContextTag = when (this) {
        TimeBucket.MORNING -> ContextTag.MORNING
        TimeBucket.AFTERNOON -> ContextTag.AFTERNOON
        TimeBucket.EVENING -> ContextTag.EVENING
        TimeBucket.NIGHT -> ContextTag.NIGHT
    }

    private val ContextTag.jsonKey: String
        get() = name.lowercase()

    private fun loadRules(): ScoringRulesDto {
        val text = context.assets.open("scoring_rules.v1.json").bufferedReader().readText()
        return json.decodeFromString(text)
    }
}

@Serializable
private data class ScoringRulesDto(
    val version: String,
    @SerialName("gap_threshold") val gapThreshold: Int,
    @SerialName("tag_weights") val tagWeights: Map<String, Int>,
)
