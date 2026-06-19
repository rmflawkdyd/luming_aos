package io.github.rmflawkdyd.luming.data.streak.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.common.truth.Truth.assertThat
import io.github.rmflawkdyd.luming.domain.model.TimeBucket
import io.github.rmflawkdyd.luming.domain.util.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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

    // ─── 스키마 키 기록 검증 ─────────────────────────────────────────────────────

    @Test fun `markCompleted - streak_current_count 키 DataStore에 기록`() = testScope.runTest {
        store.markCompleted(today)
        val prefs = dataStore.data.first()
        val persisted = prefs[intPreferencesKey("streak_current_count")]
        assertThat(persisted).isEqualTo(1)
    }

    @Test fun `markCompleted - streak_current_count 연속 2일 시 2 기록`() = testScope.runTest {
        store.markCompleted(today.minus(1, DateTimeUnit.DAY))
        store.markCompleted(today)
        val prefs = dataStore.data.first()
        val persisted = prefs[intPreferencesKey("streak_current_count")]
        assertThat(persisted).isEqualTo(2)
    }

    @Test fun `markCompleted - streak_last_completed_date 키 DataStore에 기록`() = testScope.runTest {
        store.markCompleted(today)
        val prefs = dataStore.data.first()
        val persisted = prefs[stringPreferencesKey("streak_last_completed_date")]
        assertThat(persisted).isEqualTo(today.toString())
    }

    @Test fun `markCompleted - 갭 리셋 시 streak_current_count 1 기록`() = testScope.runTest {
        store.markCompleted(today.minus(2, DateTimeUnit.DAY))
        store.markCompleted(today)
        val prefs = dataStore.data.first()
        val persisted = prefs[intPreferencesKey("streak_current_count")]
        assertThat(persisted).isEqualTo(1)
    }

    // ─── 30일 cap 회귀 방지 ──────────────────────────────────────────────────────

    @Test fun `30일 초과 연속 - count가 30에서 고정되지 않음`() = testScope.runTest {
        var last: io.github.rmflawkdyd.luming.domain.model.Streak? = null
        for (i in 99 downTo 0) {
            last = store.markCompleted(today.minus(i, DateTimeUnit.DAY))
        }
        assertThat(last!!.currentCount).isEqualTo(100)
    }

    @Test fun `getStreak - 30일 초과 연속도 정확한 count 반환`() = testScope.runTest {
        for (i in 99 downTo 0) {
            store.markCompleted(today.minus(i, DateTimeUnit.DAY))
        }
        val result = store.getStreak().first()
        assertThat(result.currentCount).isEqualTo(100)
    }

    @Test fun `markCompleted - 100일 연속 후에도 날짜 목록은 30개로 prune`() = testScope.runTest {
        for (i in 99 downTo 0) {
            store.markCompleted(today.minus(i, DateTimeUnit.DAY))
        }
        val prefs = dataStore.data.first()
        val csv = prefs[stringPreferencesKey("streak_completion_dates")]!!
        assertThat(csv.split(",")).hasSize(30)
    }

    // ─── getStreak 라이브 표시 동작 ──────────────────────────────────────────────

    @Test fun `getStreak - 오늘 미완료여도 어제까지 연속이면 직전 카운트 표시`() = testScope.runTest {
        // 어제까지 50일 연속(어제가 마지막 완료), 오늘은 아직 미완료
        for (i in 50 downTo 1) {
            store.markCompleted(today.minus(i, DateTimeUnit.DAY))
        }
        val result = store.getStreak().first()
        assertThat(result.currentCount).isEqualTo(50)
    }

    @Test fun `getStreak - 이틀 이상 갭이면 count 0`() = testScope.runTest {
        store.markCompleted(today.minus(2, DateTimeUnit.DAY))
        val result = store.getStreak().first()
        assertThat(result.currentCount).isEqualTo(0)
    }

    private class FakeClock(private val date: LocalDate) : Clock {
        override fun today() = date
        override fun timeBucket() = TimeBucket.MORNING
        override fun dayOfWeekHash() = 0
        override fun isRestHour() = false
    }
}
