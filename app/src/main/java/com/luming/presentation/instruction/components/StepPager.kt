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
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.luming.R
import com.luming.domain.model.Category
import com.luming.domain.model.Step
import com.luming.presentation.theme.LumingFocus
import com.luming.presentation.theme.LumingFocusLight
import com.luming.presentation.theme.LumingLavender
import com.luming.presentation.theme.LumingLavenderLight
import com.luming.presentation.theme.LumingMovement
import com.luming.presentation.theme.LumingMovementLight
import com.luming.presentation.theme.LumingPebble
import com.luming.presentation.theme.LumingRest
import com.luming.presentation.theme.LumingRestLight
import com.luming.presentation.theme.LumingSage
import com.luming.presentation.theme.LumingSageLight
import com.luming.presentation.theme.LumingSky
import com.luming.presentation.theme.LumingSkyLight
import com.luming.presentation.theme.LumingWalk
import com.luming.presentation.theme.LumingWalkLight

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

    // No programmatic page jumps exist (navigation is swipe-only — there are no
    // prev/next buttons), so currentStepIndex only seeds initialPage. Driving
    // animateScrollToPage from it would just re-scroll to the page the gesture
    // already settled on, risking jitter on fast swipes. Pager stays the source of
    // truth for position; the snapshotFlow below mirrors it back into the ViewModel.
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
                text = stringResource(R.string.step_indicator, page + 1, sortedSteps.size),
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

private data class CategoryVisual(val bg: Color, val fg: Color, @DrawableRes val iconRes: Int)

private fun categoryVisual(category: Category) = when (category) {
    Category.BREATHING -> CategoryVisual(LumingSkyLight, LumingSky, R.drawable.ic_cat_breathing)
    Category.STRETCH -> CategoryVisual(LumingSageLight, LumingSage, R.drawable.ic_cat_stretch)
    Category.MEDITATION -> CategoryVisual(LumingLavenderLight, LumingLavender, R.drawable.ic_cat_meditation)
    Category.WALK -> CategoryVisual(LumingWalkLight, LumingWalk, R.drawable.ic_cat_walk)
    Category.FOCUS -> CategoryVisual(LumingFocusLight, LumingFocus, R.drawable.ic_cat_focus)
    Category.MOVEMENT -> CategoryVisual(LumingMovementLight, LumingMovement, R.drawable.ic_cat_movement)
    Category.REST -> CategoryVisual(LumingRestLight, LumingRest, R.drawable.ic_cat_rest)
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
            painter = painterResource(visual.iconRes),
            contentDescription = null,
            tint = visual.fg,
            modifier = Modifier.size(44.dp),
        )
    }
}
