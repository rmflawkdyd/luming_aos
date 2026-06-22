package io.github.rmflawkdyd.luming.notification

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Test
import java.io.File

/**
 * 슬롯별 마지막 발화 날짜 영속화 검증. NotificationScheduler가 이 값으로 "오늘 이미 발화" 여부를
 * 판단하므로, 슬롯 간 독립성과 날짜 갱신이 정확해야 한다. (SlotCompletionStoreTest 패턴)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationFireStoreTest {

    private val testScope = TestScope()
    private val tempFile = File.createTempFile("notif_test", ".preferences_pb")
        .also { it.deleteOnExit() }
    private val dataStore = PreferenceDataStoreFactory.create(
        scope = testScope.backgroundScope,
        produceFile = { tempFile },
    )
    private val store = NotificationFireStore(dataStore)
    private val today = LocalDate(2026, 6, 21)
    private val yesterday = LocalDate(2026, 6, 20)

    @After fun tearDown() { tempFile.delete() }

    @Test fun `초기 상태 - 모든 슬롯 lastFired null`() = testScope.runTest {
        NotificationSlot.entries.forEach { slot ->
            assertThat(store.lastFired(slot)).isNull()
        }
    }

    @Test fun `markFired 후 lastFired가 해당 날짜 반환`() = testScope.runTest {
        store.markFired(NotificationSlot.MORNING, today)
        assertThat(store.lastFired(NotificationSlot.MORNING)).isEqualTo(today)
    }

    @Test fun `MORNING 발화 - AFTERNOON·EVENING은 null (슬롯 독립)`() = testScope.runTest {
        store.markFired(NotificationSlot.MORNING, today)
        assertThat(store.lastFired(NotificationSlot.AFTERNOON)).isNull()
        assertThat(store.lastFired(NotificationSlot.EVENING)).isNull()
    }

    @Test fun `재발화 - 최신 날짜로 갱신`() = testScope.runTest {
        store.markFired(NotificationSlot.MORNING, yesterday)
        store.markFired(NotificationSlot.MORNING, today)
        assertThat(store.lastFired(NotificationSlot.MORNING)).isEqualTo(today)
    }

    @Test fun `여러 슬롯 독립적으로 기록`() = testScope.runTest {
        store.markFired(NotificationSlot.MORNING, today)
        store.markFired(NotificationSlot.AFTERNOON, today)
        store.markFired(NotificationSlot.EVENING, today)
        assertThat(store.lastFired(NotificationSlot.MORNING)).isEqualTo(today)
        assertThat(store.lastFired(NotificationSlot.AFTERNOON)).isEqualTo(today)
        assertThat(store.lastFired(NotificationSlot.EVENING)).isEqualTo(today)
    }

    @Test fun `reset 후 모든 슬롯 null`() = testScope.runTest {
        store.markFired(NotificationSlot.MORNING, today)
        store.markFired(NotificationSlot.AFTERNOON, today)
        store.reset()
        NotificationSlot.entries.forEach { slot ->
            assertThat(store.lastFired(slot)).isNull()
        }
    }
}
