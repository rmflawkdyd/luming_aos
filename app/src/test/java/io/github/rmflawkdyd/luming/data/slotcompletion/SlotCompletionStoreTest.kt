package io.github.rmflawkdyd.luming.data.slotcompletion

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.google.common.truth.Truth.assertThat
import io.github.rmflawkdyd.luming.domain.model.TimeBucket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SlotCompletionStoreTest {

    private val testScope = TestScope()
    private val tempFile = File.createTempFile("slot_test", ".preferences_pb")
        .also { it.deleteOnExit() }
    private val dataStore = PreferenceDataStoreFactory.create(
        scope = testScope.backgroundScope,
        produceFile = { tempFile },
    )
    private val store = SlotCompletionStore(dataStore)
    private val today = LocalDate(2026, 6, 1)

    @After fun tearDown() { tempFile.delete() }

    @Test fun `초기 상태 - 모든 슬롯 isCompleted false`() = testScope.runTest {
        TimeBucket.entries.forEach { slot ->
            assertThat(store.isCompleted(slot, today)).isFalse()
        }
    }

    @Test fun `MORNING 완료 후 isCompleted true`() = testScope.runTest {
        store.markCompleted(TimeBucket.MORNING, today)
        assertThat(store.isCompleted(TimeBucket.MORNING, today)).isTrue()
    }

    @Test fun `AFTERNOON 완료 후 isCompleted true`() = testScope.runTest {
        store.markCompleted(TimeBucket.AFTERNOON, today)
        assertThat(store.isCompleted(TimeBucket.AFTERNOON, today)).isTrue()
    }

    @Test fun `EVENING 완료 후 isCompleted true`() = testScope.runTest {
        store.markCompleted(TimeBucket.EVENING, today)
        assertThat(store.isCompleted(TimeBucket.EVENING, today)).isTrue()
    }

    @Test fun `NIGHT markCompleted - 무시됨, isCompleted 항상 false`() = testScope.runTest {
        store.markCompleted(TimeBucket.NIGHT, today)
        assertThat(store.isCompleted(TimeBucket.NIGHT, today)).isFalse()
    }

    @Test fun `MORNING 완료 - AFTERNOON·EVENING은 false (크로스 슬롯 오염 없음)`() = testScope.runTest {
        store.markCompleted(TimeBucket.MORNING, today)
        assertThat(store.isCompleted(TimeBucket.AFTERNOON, today)).isFalse()
        assertThat(store.isCompleted(TimeBucket.EVENING, today)).isFalse()
    }

    @Test fun `동일 슬롯 두 번 완료 - idempotent`() = testScope.runTest {
        store.markCompleted(TimeBucket.MORNING, today)
        store.markCompleted(TimeBucket.MORNING, today)
        assertThat(store.isCompleted(TimeBucket.MORNING, today)).isTrue()
    }

    @Test fun `어제 완료 - 오늘 isCompleted false (날짜 리셋)`() = testScope.runTest {
        val yesterday = LocalDate(2026, 5, 31)
        store.markCompleted(TimeBucket.MORNING, yesterday)
        assertThat(store.isCompleted(TimeBucket.MORNING, today)).isFalse()
    }

    @Test fun `날짜 변경 후 새 슬롯 markCompleted - 이전 날 슬롯은 false`() = testScope.runTest {
        val yesterday = LocalDate(2026, 5, 31)
        store.markCompleted(TimeBucket.MORNING, yesterday)
        store.markCompleted(TimeBucket.AFTERNOON, today)
        assertThat(store.isCompleted(TimeBucket.AFTERNOON, today)).isTrue()
        assertThat(store.isCompleted(TimeBucket.MORNING, today)).isFalse()
    }

    @Test fun `여러 슬롯 동시 완료 - 각각 독립적으로 true`() = testScope.runTest {
        store.markCompleted(TimeBucket.MORNING, today)
        store.markCompleted(TimeBucket.AFTERNOON, today)
        store.markCompleted(TimeBucket.EVENING, today)
        assertThat(store.isCompleted(TimeBucket.MORNING, today)).isTrue()
        assertThat(store.isCompleted(TimeBucket.AFTERNOON, today)).isTrue()
        assertThat(store.isCompleted(TimeBucket.EVENING, today)).isTrue()
    }

    @Test fun `reset 후 모든 슬롯 false`() = testScope.runTest {
        store.markCompleted(TimeBucket.MORNING, today)
        store.markCompleted(TimeBucket.AFTERNOON, today)
        store.reset()
        assertThat(store.isCompleted(TimeBucket.MORNING, today)).isFalse()
        assertThat(store.isCompleted(TimeBucket.AFTERNOON, today)).isFalse()
    }
}
