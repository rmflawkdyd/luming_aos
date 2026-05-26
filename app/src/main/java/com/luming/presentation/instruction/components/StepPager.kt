package com.luming.presentation.instruction.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.luming.domain.model.Category
import com.luming.domain.model.Step
import com.luming.presentation.theme.LumingLavender
import com.luming.presentation.theme.LumingLavenderLight
import com.luming.presentation.theme.LumingPebble
import com.luming.presentation.theme.LumingSage
import com.luming.presentation.theme.LumingSageLight
import com.luming.presentation.theme.LumingSky
import com.luming.presentation.theme.LumingSkyLight

@Composable
fun StepPager(
    steps: List<Step>,
    currentStepIndex: Int,
    category: Category,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sortedSteps = remember(steps) { steps.sortedBy { it.order } }
    val pagerState = rememberPagerState(
        initialPage = currentStepIndex,
        pageCount = { sortedSteps.size },
    )

    LaunchedEffect(currentStepIndex) {
        pagerState.animateScrollToPage(currentStepIndex)
    }

    val currentIndexState = rememberUpdatedState(currentStepIndex)
    val onNextState = rememberUpdatedState(onNext)
    val onPreviousState = rememberUpdatedState(onPrevious)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { settledPage ->
            val idx = currentIndexState.value
            when {
                settledPage > idx -> onNextState.value()
                settledPage < idx -> onPreviousState.value()
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
    ) { page ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "${page + 1} / ${sortedSteps.size} 단계",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(modifier = Modifier.size(8.dp))
            ProgressDots(total = sortedSteps.size, current = page)

            Spacer(modifier = Modifier.weight(1f))

            CategoryCircleIcon(category = category)

            Box(modifier = Modifier.size(32.dp))

            Text(
                text = sortedSteps[page].text,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ProgressDots(total: Int, current: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            val (size, color) = when {
                index == current -> 10.dp to LumingSage
                index < current -> 8.dp to LumingSage
                else -> 8.dp to LumingPebble
            }
            Box(
                modifier = Modifier
                    .size(size)
                    .background(color = color, shape = CircleShape),
            )
        }
    }
}

private data class CategoryVisual(val bg: Color, val fg: Color, val icon: ImageVector)

private fun categoryVisual(category: Category) = when (category) {
    Category.BREATHING -> CategoryVisual(LumingSkyLight, LumingSky, Icons.Default.Air)
    Category.STRETCH -> CategoryVisual(LumingSageLight, LumingSage, Icons.Default.FitnessCenter)
    Category.MEDITATION -> CategoryVisual(LumingLavenderLight, LumingLavender, Icons.Default.SelfImprovement)
}

@Composable
private fun CategoryCircleIcon(category: Category, modifier: Modifier = Modifier) {
    val visual = categoryVisual(category)
    Box(
        modifier = modifier
            .size(96.dp)
            .background(color = visual.bg, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = visual.icon,
            contentDescription = null,
            tint = visual.fg,
            modifier = Modifier.size(44.dp),
        )
    }
}
