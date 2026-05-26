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
import androidx.compose.ui.unit.dp

@Composable
fun StepNavBar(
    isFirstStep: Boolean,
    isLastStep: Boolean,
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
            Text("이전")
        }
        if (isLastStep) {
            Button(
                onClick = onComplete,
                enabled = !isCompleting,
                modifier = Modifier.weight(2f),
            ) {
                Text("완료")
            }
        } else {
            Button(
                onClick = onNext,
                modifier = Modifier.weight(2f),
            ) {
                Text("다음")
            }
        }
    }
}
