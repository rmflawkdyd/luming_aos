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
import java.time.LocalDate

@Composable
fun SevenDayStrip(last7Days: List<Boolean>, modifier: Modifier = Modifier) {
    val today = remember { LocalDate.now() }
    val dayLabels = remember(today) {
        (6 downTo 0).map { daysAgo ->
            today.minusDays(daysAgo.toLong()).dayOfWeek.name[0].toString()
        }
    }

    Row(modifier = modifier) {
        last7Days.zip(dayLabels).forEach { (completed, label) ->
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
