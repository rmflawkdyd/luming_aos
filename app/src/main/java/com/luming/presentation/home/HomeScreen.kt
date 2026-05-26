package com.luming.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.luming.domain.model.Recommendation
import com.luming.domain.model.Streak
import com.luming.domain.model.WeatherBucket
import com.luming.presentation.home.components.ActivityCard
import com.luming.presentation.home.components.CompletionOverlay
import com.luming.presentation.home.components.RationaleBanner
import com.luming.presentation.home.components.SevenDayStrip
import com.luming.presentation.home.components.StreakRing
import com.luming.presentation.theme.LumingMist
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onActivityClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onOverlayDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) onRefresh() }

    LaunchedEffect(Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!alreadyGranted) {
            locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(uiState) {
        if (uiState is HomeUiState.WeatherAware || uiState is HomeUiState.TimeOnlyFinal) {
            isRefreshing = false
        }
    }

    val showOverlay = when (uiState) {
        is HomeUiState.WeatherAware -> uiState.showCompletionOverlay
        is HomeUiState.TimeOnlyFinal -> uiState.showCompletionOverlay
        else -> false
    }

    val pullState = rememberPullToRefreshState()

    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                onRefresh()
            },
            modifier = Modifier.fillMaxSize(),
            state = pullState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullState,
                    isRefreshing = isRefreshing,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .windowInsetsPadding(WindowInsets.statusBars),
                )
            },
        ) {
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
                is HomeUiState.TimeOnlyFinal -> HomeContent(
                    recommendations = uiState.recommendations,
                    streak = uiState.streak,
                    weatherBucket = null,
                    onActivityClick = onActivityClick,
                )
            }
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = safeInsets.calculateTopPadding() + 24.dp,
            end = 20.dp,
            bottom = safeInsets.calculateBottomPadding() + 20.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            StreakHeader(streak = streak)
        }
        if (weatherBucket != null && weatherBucket != WeatherBucket.UNKNOWN) {
            item {
                RationaleBanner(weatherBucket = weatherBucket)
            }
        }
        items(recommendations, key = { it.activity.id }) { rec ->
            ActivityCard(
                recommendation = rec,
                onClick = { onActivityClick(rec.activity.id) },
            )
        }
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
            text = "루밍",
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
