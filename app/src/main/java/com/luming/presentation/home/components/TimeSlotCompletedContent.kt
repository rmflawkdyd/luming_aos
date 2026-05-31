package com.luming.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luming.R
import com.luming.domain.model.TimeBucket
import com.luming.presentation.theme.LumingMist
import com.luming.presentation.theme.LumingSage
import com.luming.presentation.theme.LumingSageLight
import com.luming.presentation.theme.OnSurface
import com.luming.presentation.theme.OnSurfaceVariant

@Composable
fun TimeSlotCompletedContent(
    slot: TimeBucket,
    modifier: Modifier = Modifier,
) {
    val completedLabel = when (slot) {
        TimeBucket.MORNING -> stringResource(R.string.slot_completed_morning)
        TimeBucket.AFTERNOON -> stringResource(R.string.slot_completed_afternoon)
        TimeBucket.EVENING -> stringResource(R.string.slot_completed_evening)
        TimeBucket.NIGHT -> error("NIGHT slot cannot be in completed state")
    }
    val nextSlotHint = when (slot) {
        TimeBucket.MORNING -> stringResource(R.string.slot_next_morning)
        TimeBucket.AFTERNOON -> stringResource(R.string.slot_next_afternoon)
        TimeBucket.EVENING -> stringResource(R.string.slot_next_evening)
        TimeBucket.NIGHT -> error("NIGHT slot cannot be in completed state")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = "$completedLabel. $nextSlotHint" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(LumingSageLight),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = LumingSage,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = completedLabel,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = nextSlotHint,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun FooterDisclaimer(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.footer_disclaimer),
            fontSize = 11.sp,
            color = LumingMist,
        )
    }
}
