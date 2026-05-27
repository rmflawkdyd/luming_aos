package com.luming.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LocationFailedCard(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    ErrorCard(
        icon = Icons.Outlined.LocationOff,
        message = "위치 정보를 가져올 수 없어요.",
        onRetry = onRetry,
        modifier = modifier,
    )
}

@Composable
fun WeatherFailedCard(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    ErrorCard(
        icon = Icons.Outlined.CloudOff,
        message = "날씨 정보를 가져올 수 없어요.",
        onRetry = onRetry,
        modifier = modifier,
    )
}

@Composable
private fun ErrorCard(
    icon: ImageVector,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        OutlinedButton(onClick = onRetry) {
            Text("다시 시도")
        }
    }
}
