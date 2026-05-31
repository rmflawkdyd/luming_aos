package com.luming.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.DrawableRes
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.luming.R
import com.luming.domain.model.Category
import com.luming.domain.model.Recommendation
import com.luming.presentation.theme.LumingFocus
import com.luming.presentation.theme.LumingFocusLight
import com.luming.presentation.theme.LumingLavender
import com.luming.presentation.theme.LumingLavenderLight
import com.luming.presentation.theme.LumingMovement
import com.luming.presentation.theme.LumingMovementLight
import com.luming.presentation.theme.LumingRest
import com.luming.presentation.theme.LumingRestLight
import com.luming.presentation.theme.LumingSage
import com.luming.presentation.theme.LumingSageLight
import com.luming.presentation.theme.LumingShapes
import com.luming.presentation.theme.LumingSky
import com.luming.presentation.theme.LumingSkyLight
import com.luming.presentation.theme.LumingWalk
import com.luming.presentation.theme.LumingWalkLight

@Composable
fun ActivityCard(
    recommendation: Recommendation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activity = recommendation.activity
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = LumingShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CategoryIconBox(category = activity.category)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (recommendation.rationale.isNotBlank()) {
                        Text(
                            text = recommendation.rationale,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                DurationPill(durationMin = activity.durationMin, category = activity.category)
            }

        }
    }
}

private data class CategoryStyle(val bg: Color, val fg: Color, @DrawableRes val iconRes: Int)

private fun categoryStyle(category: Category) = when (category) {
    Category.BREATHING -> CategoryStyle(LumingSkyLight, LumingSky, R.drawable.ic_cat_breathing)
    Category.STRETCH -> CategoryStyle(LumingSageLight, LumingSage, R.drawable.ic_cat_stretch)
    Category.MEDITATION -> CategoryStyle(LumingLavenderLight, LumingLavender, R.drawable.ic_cat_meditation)
    Category.WALK -> CategoryStyle(LumingWalkLight, LumingWalk, R.drawable.ic_cat_walk)
    Category.FOCUS -> CategoryStyle(LumingFocusLight, LumingFocus, R.drawable.ic_cat_focus)
    Category.MOVEMENT -> CategoryStyle(LumingMovementLight, LumingMovement, R.drawable.ic_cat_movement)
    Category.REST -> CategoryStyle(LumingRestLight, LumingRest, R.drawable.ic_cat_rest)
}

@Composable
private fun CategoryIconBox(category: Category, modifier: Modifier = Modifier) {
    val style = categoryStyle(category)
    Box(
        modifier = modifier
            .size(36.dp)
            .background(color = style.bg, shape = RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(style.iconRes),
            contentDescription = null,
            tint = style.fg,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun DurationPill(durationMin: Int, category: Category, modifier: Modifier = Modifier) {
    val style = categoryStyle(category)
    Box(
        modifier = modifier
            .background(color = style.bg, shape = RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = stringResource(R.string.duration_min, durationMin),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = style.fg,
        )
    }
}
