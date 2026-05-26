package com.luming.data.streak.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
    private val KEY = stringPreferencesKey("streak_completion_dates")

    fun getStreak(): Flow<Streak> = dataStore.data.map { prefs ->
        val dates = parseDates(prefs[KEY])
        buildStreak(dates, clock.today())
    }

    suspend fun markCompleted(today: LocalDate): Streak {
        var result: Streak? = null
        dataStore.edit { prefs ->
            val dates = parseDates(prefs[KEY]).toMutableList()
            val last = dates.lastOrNull()
            when {
                last == today -> { /* idempotent: no-op */ }
                last == today.minus(1, DateTimeUnit.DAY) -> dates.add(today)
                else -> { dates.clear(); dates.add(today) }
            }
            val pruned = dates.takeLast(30)
            prefs[KEY] = pruned.joinToString(",") { it.toString() }
            result = buildStreak(pruned, today)
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
