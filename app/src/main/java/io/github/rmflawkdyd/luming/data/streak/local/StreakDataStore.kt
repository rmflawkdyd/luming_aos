package io.github.rmflawkdyd.luming.data.streak.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.rmflawkdyd.luming.domain.model.Streak
import io.github.rmflawkdyd.luming.domain.util.Clock
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
        val storedCount = prefs[KEY_CURRENT_COUNT] ?: 0
        val today = clock.today()
        val last = dates.lastOrNull()
        // currentCount is tracked independently of the (pruned, max-30) date list so the
        // streak is never capped at 30. The cached count stays visible while the streak is
        // still alive — completed today OR yesterday (today not done yet). Once a day is
        // missed the streak is broken and the ring shows 0.
        val count = if (last == today || last == today.minus(1, DateTimeUnit.DAY)) storedCount else 0
        Streak(count, last, computeLast7Days(dates, today))
    }

    suspend fun markCompleted(today: LocalDate): Streak {
        var result: Streak? = null
        dataStore.edit { prefs ->
            val dates = parseDates(prefs[KEY_COMPLETION_DATES]).toMutableList()
            val prevCount = prefs[KEY_CURRENT_COUNT] ?: 0
            val last = dates.lastOrNull()
            // Count is computed incrementally from the known transition, NOT by walking the
            // date list — that keeps it independent of the 30-date storage prune.
            val count = when {
                last == today -> prevCount.coerceAtLeast(1)              // idempotent same-day
                last == today.minus(1, DateTimeUnit.DAY) -> {            // consecutive day
                    dates.add(today)
                    prevCount + 1
                }
                else -> {                                               // first / gap / clock skew
                    dates.clear()
                    dates.add(today)
                    1
                }
            }
            // Prune the date list to the last 30 — only used for last7Days + diagnostics.
            val pruned = dates.takeLast(30)
            val streak = Streak(count, today, computeLast7Days(pruned, today))
            prefs[KEY_COMPLETION_DATES] = pruned.joinToString(",") { it.toString() }
            prefs[KEY_CURRENT_COUNT] = count
            prefs[KEY_LAST_COMPLETED_DATE] = today.toString()
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

    private fun computeLast7Days(dates: List<LocalDate>, today: LocalDate): List<Boolean> =
        (6 downTo 0).map { offset ->
            dates.contains(today.minus(offset, DateTimeUnit.DAY))
        }
}
