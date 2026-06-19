package io.github.rmflawkdyd.luming.data.util

import io.github.rmflawkdyd.luming.domain.model.TimeBucket
import io.github.rmflawkdyd.luming.domain.util.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlinx.datetime.Clock as KtClock

/**
 * 시각 공급자(clock)와 타임존을 주입받아 모든 시각 계산을 그로부터 파생한다.
 * 프로덕션은 무인자 [Inject] 생성자가 시스템 시계/타임존을 사용하고,
 * 테스트는 internal 생성자로 고정 시각을 주입해 경계값을 결정적으로 검증한다.
 */
class ClockImpl internal constructor(
    private val clock: KtClock,
    private val timeZone: TimeZone,
) : Clock {

    @Inject constructor() : this(KtClock.System, TimeZone.currentSystemDefault())

    private fun nowDateTime() = clock.now().toLocalDateTime(timeZone)

    override fun today(): LocalDate = nowDateTime().date

    override fun timeBucket(): TimeBucket {
        val hour = nowDateTime().hour
        return when {
            hour in 5..11 -> TimeBucket.MORNING
            hour in 12..16 -> TimeBucket.AFTERNOON
            hour in 17..20 -> TimeBucket.EVENING
            else -> TimeBucket.NIGHT
        }
    }

    override fun isRestHour(): Boolean = nowDateTime().hour in 0..4

    override fun dayOfWeekHash(): Int =
        today().dayOfWeek.value % 7
}
