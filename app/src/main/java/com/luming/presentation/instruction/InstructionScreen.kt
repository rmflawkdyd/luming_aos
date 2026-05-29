package com.luming.presentation.instruction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.luming.presentation.instruction.components.StepPager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionScreen(
    uiState: InstructionUiState?,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onStart: () -> Unit,
    onComplete: (elapsedMs: Long) -> Unit,
    onConfirmWarning: () -> Unit,
    onDismissWarning: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Ticker: recomputed every second from startedAt so background time is included
    var elapsedMs by remember { mutableStateOf(0L) }
    val timerStartedAt = uiState.timerStartedAt
    LaunchedEffect(timerStartedAt) {
        val startedAt = timerStartedAt ?: run { elapsedMs = 0L; return@LaunchedEffect }
        while (true) {
            elapsedMs = System.currentTimeMillis() - startedAt
            delay(1_000L)
        }
    }

    // TimerWarningDialog
    if (uiState.showTimerWarning) {
        val thresholdMs = (uiState.activity.durationMin * 60_000L * 0.8).toLong()
        val remainingMs = (thresholdMs - elapsedMs).coerceAtLeast(0L)
        val remainingMins = ((remainingMs + 59_999L) / 60_000L).coerceAtLeast(1L)
        AlertDialog(
            onDismissRequest = onDismissWarning,
            title = { Text("아직 완료하지 않으셨나요?") },
            text = { Text("아직 ${remainingMins}분 남았어요. 지금 완료할까요?") },
            confirmButton = {
                TextButton(onClick = onConfirmWarning) { Text("완료") }
            },
            dismissButton = {
                TextButton(onClick = onDismissWarning) { Text("계속하기") }
            },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.activity.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "${uiState.activity.durationMin}분",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            AnimatedVisibility(
                visible = uiState.isTimerRunning,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "${formatMs(elapsedMs)} / ${formatDuration(uiState.activity.durationMin)}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .semantics { contentDescription = "경과 시간 ${formatMs(elapsedMs)}, 전체 ${uiState.activity.durationMin}분" },
                )
            }

            StepPager(
                steps = uiState.activity.steps,
                currentStepIndex = uiState.currentStepIndex,
                category = uiState.activity.category,
                onNext = onNext,
                onPrevious = onPrevious,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )

            if (uiState.isTimerRunning) {
                Button(
                    onClick = { onComplete(elapsedMs) },
                    enabled = !uiState.isCompleting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                ) {
                    Text("완료")
                }
            } else {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .semantics { contentDescription = "타이머 시작" },
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("시작")
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

private fun formatDuration(durationMin: Int): String = "%d:00".format(durationMin)
