package io.github.rmflawkdyd.luming.presentation.instruction.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.rmflawkdyd.luming.presentation.theme.LumingSage
import io.github.rmflawkdyd.luming.presentation.theme.OnSurface
import io.github.rmflawkdyd.luming.presentation.theme.OnSurfaceVariant
import io.github.rmflawkdyd.luming.presentation.theme.SurfaceWhite

// luming.pen molecule/LumingDialog 디자인 토큰
private val DialogSecondaryBg = Color(0xFFF1F1EE)

/**
 * luming.pen `molecule/LumingDialog` 디자인을 그대로 옮긴 커스텀 다이얼로그.
 * 카드(흰색 24dp 라운드) + 아이콘 배지 + 제목/본문 + 세로 버튼 2개.
 */
@Composable
fun LumingDialog(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    message: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit,
    onDismissRequest: () -> Unit,
    secondaryBackground: Color = DialogSecondaryBg,
    secondaryTextColor: Color = OnSurfaceVariant,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceWhite)
                .padding(start = 24.dp, top = 28.dp, end = 24.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // 아이콘 배지
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp),
                )
            }

            // 제목 + 본문
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    color = OnSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 23.sp,
                    letterSpacing = (-0.36).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = message,
                    color = OnSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    letterSpacing = (-0.14).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // 버튼 (세로 2개)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DialogButton(
                    label = primaryLabel,
                    onClick = onPrimary,
                    background = LumingSage,
                    textColor = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
                DialogButton(
                    label = secondaryLabel,
                    onClick = onSecondary,
                    background = secondaryBackground,
                    textColor = secondaryTextColor,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun DialogButton(
    label: String,
    onClick: () -> Unit,
    background: Color,
    textColor: Color,
    fontWeight: FontWeight,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(PaddingValues(horizontal = 16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = fontWeight,
            letterSpacing = (-0.16).sp,
        )
    }
}
