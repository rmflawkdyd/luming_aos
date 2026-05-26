package com.luming.data.recommender

import android.util.Log
import com.luming.domain.model.Activity
import com.luming.domain.model.ContextSnapshot
import com.luming.domain.model.TimeBucket
import com.luming.domain.model.WeatherBucket
import org.tensorflow.lite.Interpreter
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TFLiteSelector @Inject constructor(
    private val interpreter: Interpreter?,
    @param:Named("activityIndexTable") private val activityIndexTable: Map<String, Int>,
) {
    val isLoaded: Boolean get() = interpreter != null

    fun pickIndex(ctx: ContextSnapshot, topK: List<Pair<Activity, Int>>): Int {
        val interp = interpreter ?: return 0
        return try {
            val contextOneHot = buildContextOneHot(ctx)
            val candidateIndices = buildCandidateIndices(topK)

            val inputs = arrayOf<Any>(
                arrayOf(contextOneHot),
                arrayOf(candidateIndices),
            )
            val logits = Array(1) { FloatArray(3) }
            val outputs = hashMapOf<Int, Any>(0 to logits)

            interp.runForMultipleInputsOutputs(inputs, outputs)

            val result = logits[0].copyOf()
            candidateIndices.forEachIndexed { i, idx ->
                if (idx == PAD_INDEX) result[i] = Float.NEGATIVE_INFINITY
            }
            result.indices.maxByOrNull { result[it] } ?: 0
        } catch (e: Exception) {
            Log.w(TAG, "Selector inference failed, falling back to top-0", e)
            0
        }
    }

    private fun buildContextOneHot(ctx: ContextSnapshot): FloatArray {
        val arr = FloatArray(17)
        // time_bucket one-hot: indices 0-3 (MORNING=0, AFTERNOON=1, EVENING=2, NIGHT=3)
        arr[ctx.timeBucket.ordinal] = 1f
        // weather_bucket one-hot: indices 4-9 (CLEAR=0, CLOUDY=1, RAINY=2, HOT=3, COLD=4, UNKNOWN=5)
        arr[4 + ctx.weatherBucket.ordinal] = 1f
        // day_of_week_hash one-hot: indices 10-16
        arr[10 + ctx.dayOfWeekHash.coerceIn(0, 6)] = 1f
        return arr
    }

    private fun buildCandidateIndices(topK: List<Pair<Activity, Int>>): IntArray {
        val arr = IntArray(3) { PAD_INDEX }
        topK.forEachIndexed { i, (activity, _) ->
            arr[i] = activityIndexTable[activity.id] ?: PAD_INDEX
        }
        return arr
    }

    private companion object {
        const val PAD_INDEX = 0
        const val TAG = "TFLiteSelector"
    }
}
