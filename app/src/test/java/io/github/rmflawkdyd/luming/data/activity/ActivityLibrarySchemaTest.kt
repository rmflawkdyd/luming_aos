package io.github.rmflawkdyd.luming.data.activity

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import io.github.rmflawkdyd.luming.data.activity.local.dto.ActivityLibraryDto
import io.github.rmflawkdyd.luming.domain.model.Activity
import io.github.rmflawkdyd.luming.domain.model.Category
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], application = android.app.Application::class)
class ActivityLibrarySchemaTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val json = Json { ignoreUnknownKeys = true }
    private val activities: List<Activity> by lazy {
        val text = context.assets.open("activities.v1.json").bufferedReader().readText()
        json.decodeFromString<ActivityLibraryDto>(text).activities
    }

    @Test fun `총 활동 30개 이상`() {
        assertThat(activities.size).isAtLeast(30)
    }

    @Test fun `카테고리당 8개 이상`() {
        val byCategory = activities.groupBy { it.category }
        Category.entries.forEach { category ->
            assertWithMessage("category $category")
                .that(byCategory[category]?.size ?: 0)
                .isAtLeast(8)
        }
    }

    @Test fun `모든 활동에 id 존재`() {
        activities.forEach { activity ->
            assertWithMessage("activity id empty").that(activity.id).isNotEmpty()
        }
    }

    @Test fun `모든 활동 duration 5-20분 범위`() {
        activities.forEach { activity ->
            assertWithMessage("activity ${activity.id}")
                .that(activity.durationMin)
                .isIn(5..20)
        }
    }

    @Test fun `모든 활동에 steps 존재`() {
        activities.forEach { activity ->
            assertWithMessage("activity ${activity.id}")
                .that(activity.steps)
                .isNotEmpty()
        }
    }

    @Test fun `모든 활동에 contextTags 존재`() {
        activities.forEach { activity ->
            assertWithMessage("activity ${activity.id}")
                .that(activity.contextTags)
                .isNotEmpty()
        }
    }
}
