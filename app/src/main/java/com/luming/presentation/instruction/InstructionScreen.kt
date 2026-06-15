package com.luming.presentation.instruction

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.luming.R
import com.luming.presentation.instruction.components.LumingDialog
import com.luming.presentation.instruction.components.StepPager
import com.luming.presentation.theme.LumingSage
import com.luming.presentation.theme.LumingSageLight
import com.luming.presentation.theme.LumingTerracotta
import kotlinx.coroutines.delay

// luming.pen screen/Instruction-AbortDialog 아이콘 배지/보조 버튼 배경 (#F6EBE5)
private val DialogAbortBadgeBg = Color(0xFFF6EBE5)

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
    onConfirmAbort: () -> Unit,
    onDismissAbort: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 시스템 백(제스처/하드웨어)도 툴바 백과 동일 경로로 통합 (스펙 §8.2)
    BackHandler(onBack = onBack)

    if (uiState == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Ticker: recomputed every second from startedAt so background time is included
    var elapsedMs by remember { mutableStateOf(0L) }
    val timerStartedAt = uiState.timerStartedAt
    val targetMs = uiState.activity.durationMin * 60_000L
    LaunchedEffect(timerStartedAt) {
        val startedAt = timerStartedAt ?: run { elapsedMs = 0L; return@LaunchedEffect }
        while (true) {
            elapsedMs = System.currentTimeMillis() - startedAt
            if (elapsedMs >= targetMs) {
                elapsedMs = targetMs
                onComplete(elapsedMs)
                break
            }
            delay(1_000L)
        }
    }

    // TimerWarningDialog — luming.pen screen/Instruction-WarnDialog
    if (uiState.showTimerWarning) {
        val thresholdMs = (uiState.activity.durationMin * 60_000L * 0.8).toLong()
        val remainingMs = (thresholdMs - elapsedMs).coerceAtLeast(0L)
        val remainingMins = ((remainingMs + 59_999L) / 60_000L).coerceAtLeast(1L)
        LumingDialog(
            icon = Icons.Outlined.Schedule,
            iconTint = LumingSage,
            iconBackground = LumingSageLight,
            title = stringResource(R.string.dialog_early_complete_title),
            message = stringResource(R.string.dialog_early_complete_message, remainingMins),
            primaryLabel = stringResource(R.string.action_continue),
            onPrimary = onDismissWarning,
            secondaryLabel = stringResource(R.string.action_complete),
            onSecondary = onConfirmWarning,
            secondaryTextColor = LumingSage,
            onDismissRequest = onDismissWarning,
        )
    }

    // AbortWarningDialog — 타이머 진행 중 이탈 확인 (스펙 §2.2 aborting) / luming.pen screen/Instruction-AbortDialog
    if (uiState.showAbortWarning) {
        LumingDialog(
            icon = Icons.Outlined.WarningAmber,
            iconTint = LumingTerracotta,
            iconBackground = DialogAbortBadgeBg,
            title = stringResource(R.string.dialog_abort_title),
            message = stringResource(R.string.dialog_abort_message),
            primaryLabel = stringResource(R.string.action_continue),
            onPrimary = onDismissAbort,
            secondaryLabel = stringResource(R.string.action_abort),
            onSecondary = onConfirmAbort,
            secondaryBackground = DialogAbortBadgeBg,
            secondaryTextColor = LumingTerracotta,
            onDismissRequest = onDismissAbort,
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                    ),
                title = {
                    Column {
                        Text(
                            text = uiState.activity.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.duration_min, uiState.activity.durationMin),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        val elapsedTimeCd = stringResource(R.string.cd_elapsed_time, formatMs(elapsedMs), uiState.activity.durationMin)
        val timerStartCd = stringResource(R.string.cd_timer_start)
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
                        .semantics { contentDescription = elapsedTimeCd },
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
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                ) {
                    Text(stringResource(R.string.action_complete))
                }
            } else {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .height(52.dp)
                        .semantics { contentDescription = timerStartCd },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_start))
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
