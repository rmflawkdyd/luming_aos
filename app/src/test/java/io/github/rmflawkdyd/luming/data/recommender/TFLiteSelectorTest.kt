package io.github.rmflawkdyd.luming.data.recommender

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import io.github.rmflawkdyd.luming.domain.model.ContextSnapshot
import io.github.rmflawkdyd.luming.domain.model.TimeBucket
import io.github.rmflawkdyd.luming.domain.model.WeatherBucket
import org.junit.Test

class TFLiteSelectorTest {

    private val selector = TFLiteSelector(interpreter = null, activityIndexTable = emptyMap())

    private val ctx = ContextSnapshot(
        timeBucket = TimeBucket.MORNING,
        weatherBucket = WeatherBucket.CLEAR,
        dayOfWeekHash = 0,
        isPrecipitating = false,
    )

    @Test fun `interpreter null - 항상 0 반환`() {
        repeat(100) { iteration ->
            assertWithMessage("iteration $iteration")
                .that(selector.pickIndex(ctx, emptyList()))
                .isEqualTo(0)
        }
    }

    @Test fun `interpreter null - isLoaded false`() {
        assertThat(selector.isLoaded).isFalse()
    }

    @Test fun `모든 TimeBucket에서 null interpreter - 항상 0`() {
        TimeBucket.entries.forEach { bucket ->
            assertWithMessage("bucket $bucket")
                .that(selector.pickIndex(ctx.copy(timeBucket = bucket), emptyList()))
                .isEqualTo(0)
        }
    }

    @Test fun `모든 WeatherBucket에서 null interpreter - 항상 0`() {
        WeatherBucket.entries.forEach { bucket ->
            assertWithMessage("bucket $bucket")
                .that(selector.pickIndex(ctx.copy(weatherBucket = bucket), emptyList()))
                .isEqualTo(0)
        }
    }
}
