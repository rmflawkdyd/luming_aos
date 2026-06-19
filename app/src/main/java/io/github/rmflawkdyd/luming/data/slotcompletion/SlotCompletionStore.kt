package io.github.rmflawkdyd.luming.data.slotcompletion

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.rmflawkdyd.luming.domain.model.TimeBucket
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SlotCompletionStore @Inject constructor(
    @param:Named("slot") private val dataStore: DataStore<Preferences>,
) {
    private val KEY_DATE = stringPreferencesKey("slot_date")
    private val KEY_COMPLETED = stringPreferencesKey("slot_completed")

    suspend fun isCompleted(slot: TimeBucket, today: LocalDate): Boolean {
        if (slot == TimeBucket.NIGHT) return false
        val prefs = dataStore.data.first()
        val storedDate = prefs[KEY_DATE] ?: return false
        if (storedDate != today.toString()) return false
        return slot in parseSlots(prefs[KEY_COMPLETED])
    }

    suspend fun markCompleted(slot: TimeBucket, today: LocalDate) {
        if (slot == TimeBucket.NIGHT) return
        dataStore.edit { prefs ->
            val storedDate = prefs[KEY_DATE]
            val current = if (storedDate == today.toString()) {
                parseSlots(prefs[KEY_COMPLETED]).toMutableSet()
            } else {
                mutableSetOf()
            }
            current.add(slot)
            prefs[KEY_DATE] = today.toString()
            prefs[KEY_COMPLETED] = current.joinToString(",") { it.name.lowercase() }
        }
    }

    @VisibleForTesting
    suspend fun reset() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_DATE)
            prefs.remove(KEY_COMPLETED)
        }
    }

    private fun parseSlots(raw: String?): Set<TimeBucket> {
        if (raw.isNullOrBlank()) return emptySet()
        return raw.split(",")
            .mapNotNull { name ->
                runCatching { TimeBucket.valueOf(name.trim().uppercase()) }.getOrNull()
                    ?.takeIf { it != TimeBucket.NIGHT }
            }
            .toSet()
    }
}
