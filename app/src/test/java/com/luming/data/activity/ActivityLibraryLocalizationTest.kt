package com.luming.data.activity

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.luming.data.activity.local.dto.ActivityLibraryDto
import com.luming.domain.model.Activity
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guards parity between the Korean (default) and English activity libraries. Only the
 * `name` and step `text` fields are translated; every structural field (id, category,
 * duration, ordering, context tags) must match so the locale-aware loader can swap files
 * transparently. See ActivityLocalDataSource.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], application = android.app.Application::class)
class ActivityLibraryLocalizationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val json = Json { ignoreUnknownKeys = true }

    private fun load(asset: String): List<Activity> {
        val text = context.assets.open(asset).bufferedReader().readText()
        return json.decodeFromString<ActivityLibraryDto>(text).activities
    }

    private val ko by lazy { load("activities.v1.json") }
    private val en by lazy { load("activities.en.v1.json") }

    @Test fun `같은 개수와 같은 id 순서`() {
        assertThat(en.map { it.id }).isEqualTo(ko.map { it.id })
    }

    @Test fun `구조 필드 일치 - category duration steps contextTags`() {
        ko.zip(en).forEach { (k, e) ->
            assertWithMessage("category ${k.id}").that(e.category).isEqualTo(k.category)
            assertWithMessage("duration ${k.id}").that(e.durationMin).isEqualTo(k.durationMin)
            assertWithMessage("contextTags ${k.id}").that(e.contextTags).isEqualTo(k.contextTags)
            assertWithMessage("step count ${k.id}").that(e.steps.size).isEqualTo(k.steps.size)
            assertWithMessage("step order ${k.id}")
                .that(e.steps.map { it.order })
                .isEqualTo(k.steps.map { it.order })
        }
    }

    @Test fun `영문 name과 step text 비어있지 않음`() {
        en.forEach { activity ->
            assertWithMessage("name ${activity.id}").that(activity.name).isNotEmpty()
            activity.steps.forEach { step ->
                assertWithMessage("step text ${activity.id}#${step.order}")
                    .that(step.text)
                    .isNotEmpty()
            }
        }
    }
}
