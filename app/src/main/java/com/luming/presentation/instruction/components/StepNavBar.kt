package com.luming.presentation.instruction.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.luming.R

@Composable
fun StepNavBar(
    isFirstStep: Boolean,
    isLastStep: Boolean,
    isTimerStarted: Boolean,
    isCompleting: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = !isFirstStep,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.action_previous))
        }
        if (isLastStep) {
            val completeEnabled = isTimerStarted && !isCompleting
            Button(
                onClick = onComplete,
                enabled = completeEnabled,
                modifier = Modifier
                    .weight(2f)
                    .alpha(if (completeEnabled) 1f else 0.4f),
            ) {
                Text(stringResource(R.string.action_complete))
            }
        } else {
            Button(
                onClick = onNext,
                modifier = Modifier.weight(2f),
            ) {
                Text(stringResource(R.string.action_next))
            }
        }
    }
}
