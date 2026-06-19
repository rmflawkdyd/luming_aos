package io.github.rmflawkdyd.luming.presentation.home.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.rmflawkdyd.luming.R
import io.github.rmflawkdyd.luming.domain.model.WeatherBucket

@Composable
fun RationaleBanner(weatherBucket: WeatherBucket?, modifier: Modifier = Modifier) {
    val (text, icon) = when (weatherBucket) {
        WeatherBucket.CLEAR -> stringResource(R.string.rationale_clear) to Icons.Default.WbSunny
        WeatherBucket.CLOUDY -> stringResource(R.string.rationale_cloudy) to Icons.Default.Cloud
        WeatherBucket.RAINY -> stringResource(R.string.rationale_rainy) to Icons.Default.WaterDrop
        WeatherBucket.HOT -> stringResource(R.string.rationale_hot) to Icons.Default.WbSunny
        WeatherBucket.COLD -> stringResource(R.string.rationale_cold) to Icons.Default.AcUnit
        WeatherBucket.UNKNOWN, null -> stringResource(R.string.rationale_unknown) to Icons.Default.AccessTime
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
