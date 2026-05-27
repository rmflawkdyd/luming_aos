package com.luming.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luming.domain.model.WeatherBucket

@Composable
fun RationaleBanner(weatherBucket: WeatherBucket?, modifier: Modifier = Modifier) {
    val (text, icon) = when (weatherBucket) {
        WeatherBucket.CLEAR -> "맑은 날씨에 어울리는 활동이에요" to Icons.Default.WbSunny
        WeatherBucket.CLOUDY -> "흐린 날에도 할 수 있는 활동이에요" to Icons.Default.Cloud
        WeatherBucket.RAINY -> "실내에서 즐길 수 있는 활동이에요" to Icons.Default.WaterDrop
        WeatherBucket.HOT -> "더운 날엔 실내 활동이 좋아요" to Icons.Default.WbSunny
        WeatherBucket.COLD -> "추운 날엔 몸을 따뜻하게 풀어보세요" to Icons.Default.AcUnit
        WeatherBucket.UNKNOWN, null -> "시간대에 맞는 활동이에요" to Icons.Default.AccessTime
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
