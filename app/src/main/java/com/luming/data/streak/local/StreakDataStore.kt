package com.luming.data.streak.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.luming.domain.model.Streak
import com.luming.domain.util.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val clock: Clock,
) {
    private val KEY_COMPLETION_DATES = stringPreferencesKey("streak_completion_dates")
    private val KEY_CURRENT_COUNT = intPreferencesKey("streak_current_count")
    private val KEY_LAST_COMPLETED_DATE = stringPreferencesKey("streak_last_completed_date")

    fun getStreak(): Flow<Streak> = dataStore.data.map { prefs ->
        val dates = parseDates(prefs[KEY_COMPLETION_DATES])
        buildStreak(dates, clock.today())
    }

    suspend fun markCompleted(today: LocalDate): Streak {
        var result: Streak? = null
        dataStore.edit { prefs ->
            val dates = parseDates(prefs[KEY_COMPLETION_DATES]).toMutableList()
            val last = dates.lastOrNull()
            when {
                last == today -> { /* idempotent: no-op */ }
                last == today.minus(1, DateTimeUnit.DAY) -> dates.add(today)
                else -> { dates.clear(); dates.add(today) }
            }
            val pruned = dates.takeLast(30)
            val streak = buildStreak(pruned, today)
            prefs[KEY_COMPLETION_DATES] = pruned.joinToString(",") { it.toString() }
            prefs[KEY_CURRENT_COUNT] = streak.currentCount
            if (streak.lastCompletedDate != null) {
                prefs[KEY_LAST_COMPLETED_DATE] = streak.lastCompletedDate.toString()
            } else {
                prefs.remove(KEY_LAST_COMPLETED_DATE)
            }
            result = streak
        }
        return result!!
    }

    private fun parseDates(raw: String?): List<LocalDate> =
        raw?.split(",")
            ?.filter { it.isNotBlank() }
            ?.map { LocalDate.parse(it) }
            ?.sorted()
            ?: emptyList()

    private fun buildStreak(dates: List<LocalDate>, today: LocalDate): Streak {
        var count = 0
        var cursor = today
        while (dates.contains(cursor)) {
            count++
            cursor = cursor.minus(1, DateTimeUnit.DAY)
        }
        val last7 = (6 downTo 0).map { offset ->
            dates.contains(today.minus(offset, DateTimeUnit.DAY))
        }
        return Streak(count, dates.lastOrNull(), last7)
    }
}
