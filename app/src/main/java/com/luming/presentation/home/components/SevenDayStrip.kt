package com.luming.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luming.presentation.theme.DotCompleted
import com.luming.presentation.theme.LumingPebble
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun SevenDayStrip(last7Days: List<Boolean>, modifier: Modifier = Modifier) {
    val today = remember { LocalDate.now() }
    // 요일 컬럼은 월~일 고정. 최근 7일 데이터를 각 실제 요일 칸에 매핑한다.
    // last7Days: index 0 = 6일 전 ... index 6 = 오늘 (오래된 -> 최신 순)
    val completedByWeekday = remember(today, last7Days) {
        (0..6).associate { i ->
            val date = today.minusDays((6 - i).toLong())
            date.dayOfWeek to (last7Days.getOrNull(i) ?: false)
        }
    }
    val weekdayColumns = remember {
        listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY,
        )
    }

    Row(modifier = modifier) {
        weekdayColumns.forEach { weekday ->
            val completed = completedByWeekday[weekday] ?: false
            val label = weekday.name[0].toString()
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (completed) DotCompleted else LumingPebble,
                            shape = CircleShape,
                        ),
                )
                Text(
                    text = label,
                    fontSize = 9.sp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
