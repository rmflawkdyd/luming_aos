package io.github.rmflawkdyd.luming.notification

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * 슬롯별 "마지막으로 발화한 날짜"를 영속화한다. NotificationScheduler가 예약 직전에 조회해
 * 오늘 이미 발화한 슬롯은 다음 날로 미뤄 같은 시간대 중복 발화를 차단한다.
 * (SlotCompletionStore와 동일한 DataStore 패턴, 키만 슬롯별 분리)
 */
@Singleton
class NotificationFireStore @Inject constructor(
    @param:Named("notif") private val dataStore: DataStore<Preferences>,
) {
    suspend fun lastFired(slot: NotificationSlot): LocalDate? {
        val raw = dataStore.data.first()[keyFor(slot)] ?: return null
        return runCatching { LocalDate.parse(raw) }.getOrNull()
    }

    suspend fun markFired(slot: NotificationSlot, today: LocalDate) {
        dataStore.edit { prefs -> prefs[keyFor(slot)] = today.toString() }
    }

    @VisibleForTesting
    suspend fun reset() {
        dataStore.edit { prefs ->
            NotificationSlot.entries.forEach { prefs.remove(keyFor(it)) }
        }
    }

    private fun keyFor(slot: NotificationSlot) =
        stringPreferencesKey("notif_last_fired_${slot.name}")
}
