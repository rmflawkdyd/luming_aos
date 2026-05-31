package com.luming.data.streak.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.google.common.truth.Truth.assertThat
import com.luming.domain.model.TimeBucket
import com.luming.domain.util.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.After
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class StreakDataStoreTest {

    private val testScope = TestScope()
    private val tempFile = File.createTempFile("streak_test", ".preferences_pb")
        .also { it.deleteOnExit() }
    private val dataStore = PreferenceDataStoreFactory.create(
        scope = testScope.backgroundScope,
        produceFile = { tempFile },
    )

    private val today = LocalDate(2026, 5, 31)
    private val store = StreakDataStore(dataStore, FakeClock(today))

    @After fun tearDown() { tempFile.delete() }

    @Test fun `같은 날 두 번 호출 - streak 1 유지`() = testScope.runTest {
        store.markCompleted(today)
        val result = store.markCompleted(today)
        assertThat(result.currentCount).isEqualTo(1)
    }

    @Test fun `하루 뒤 호출 - streak 2`() = testScope.runTest {
        store.markCompleted(today.minus(1, DateTimeUnit.DAY))
        val result = store.markCompleted(today)
        assertThat(result.currentCount).isEqualTo(2)
    }

    @Test fun `이틀 이상 갭 - streak 리셋 1`() = testScope.runTest {
        store.markCompleted(today.minus(2, DateTimeUnit.DAY))
        val result = store.markCompleted(today)
        assertThat(result.currentCount).isEqualTo(1)
    }

    @Test fun `첫 완료 - streak 1`() = testScope.runTest {
        val result = store.markCompleted(today)
        assertThat(result.currentCount).isEqualTo(1)
    }

    @Test fun `오늘 완료 - last7Days 마지막 항목 true`() = testScope.runTest {
        val result = store.markCompleted(today)
        assertThat(result.last7Days.last()).isTrue()
    }

    @Test fun `last7Days 항상 7개`() = testScope.runTest {
        val result = store.markCompleted(today)
        assertThat(result.last7Days).hasSize(7)
    }

    @Test fun `lastCompletedDate 오늘로 갱신`() = testScope.runTest {
        val result = store.markCompleted(today)
        assertThat(result.lastCompletedDate).isEqualTo(today)
    }

    private class FakeClock(private val date: LocalDate) : Clock {
        override fun today() = date
        override fun timeBucket() = TimeBucket.MORNING
        override fun dayOfWeekHash() = 0
    }
}
