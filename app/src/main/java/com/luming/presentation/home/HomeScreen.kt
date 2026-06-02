package com.luming.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luming.R
import com.luming.domain.model.Recommendation
import com.luming.domain.model.Streak
import com.luming.domain.model.TimeBucket
import com.luming.domain.model.WeatherBucket
import com.luming.presentation.home.components.ActivityCard
import com.luming.presentation.home.components.CompletionOverlay
import com.luming.presentation.home.components.FooterDisclaimer
import com.luming.presentation.home.components.LocationFailedCard
import com.luming.presentation.home.components.RationaleBanner
import com.luming.presentation.home.components.SevenDayStrip
import com.luming.presentation.home.components.StreakRing
import com.luming.presentation.home.components.TimeSlotCompletedContent
import com.luming.presentation.home.components.WeatherFailedCard
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onActivityClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onOverlayDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showOverlay = uiState.showsOverlay()

    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            HomeUiState.Empty -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.TimeOnly -> HomeContent(
                recommendations = uiState.recommendations,
                streak = uiState.streak,
                weatherBucket = null,
                onActivityClick = onActivityClick,
            )
            is HomeUiState.WeatherAware -> HomeContent(
                recommendations = uiState.recommendations,
                streak = uiState.streak,
                weatherBucket = uiState.weatherBucket,
                onActivityClick = onActivityClick,
            )
            HomeUiState.LocationFailed -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LocationFailedCard(onRetry = onRefresh)
                }
            }
            HomeUiState.WeatherFailed -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    WeatherFailedCard(onRetry = onRefresh)
                }
            }
            is HomeUiState.CompletedSlot -> CompletedSlotContent(
                slot = uiState.slot,
                streak = uiState.streak,
                weatherBucket = uiState.weatherBucket,
            )
        }

        if (showOverlay) {
            CompletionOverlay(
                onDismiss = onOverlayDismissed,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun HomeContent(
    recommendations: List<Recommendation>,
    streak: Streak,
    weatherBucket: WeatherBucket?,
    onActivityClick: (String) -> Unit,
) {
    val safeInsets = WindowInsets.safeDrawing.asPaddingValues()
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = safeInsets.calculateTopPadding() + 24.dp,
                end = 20.dp,
                bottom = safeInsets.calculateBottomPadding() + 48.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                StreakHeader(streak = streak)
            }
            item {
                RationaleBanner(weatherBucket = weatherBucket)
            }
            items(recommendations, key = { it.activity.id }) { rec ->
                ActivityCard(
                    recommendation = rec,
                    onClick = { onActivityClick(rec.activity.id) },
                )
            }
        }
        FooterDisclaimer(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = safeInsets.calculateBottomPadding() + 8.dp),
        )
    }
}

@Composable
private fun CompletedSlotContent(
    slot: TimeBucket,
    streak: Streak,
    weatherBucket: WeatherBucket?,
    modifier: Modifier = Modifier,
) {
    val safeInsets = WindowInsets.safeDrawing.asPaddingValues()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = 20.dp,
                top = safeInsets.calculateTopPadding() + 24.dp,
                end = 20.dp,
                bottom = safeInsets.calculateBottomPadding() + 20.dp,
            ),
    ) {
        StreakHeader(streak = streak)
        Spacer(modifier = Modifier.height(32.dp))
        RationaleBanner(weatherBucket = weatherBucket)
        Spacer(modifier = Modifier.height(16.dp))
        TimeSlotCompletedContent(
            slot = slot,
            modifier = Modifier.weight(1f),
        )
        FooterDisclaimer(modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
    }
}

@Composable
private fun StreakHeader(streak: Streak) {
    val today = remember { LocalDate.now() }
    val dateText = remember(today) {
        "${today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}, " +
            "${today.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${today.dayOfMonth}"
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dateText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            StreakRing(streakCount = streak.currentCount)
            SevenDayStrip(
                last7Days = streak.last7Days,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
