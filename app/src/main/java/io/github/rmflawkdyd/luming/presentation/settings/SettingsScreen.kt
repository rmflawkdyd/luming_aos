package io.github.rmflawkdyd.luming.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.rmflawkdyd.luming.R
import io.github.rmflawkdyd.luming.presentation.theme.LumingClay
import io.github.rmflawkdyd.luming.presentation.theme.LumingClayLight
import io.github.rmflawkdyd.luming.presentation.theme.LumingMist
import io.github.rmflawkdyd.luming.presentation.theme.LumingPebble
import io.github.rmflawkdyd.luming.presentation.theme.LumingSage
import io.github.rmflawkdyd.luming.presentation.theme.LumingSlate
import io.github.rmflawkdyd.luming.presentation.theme.LumingSlateBg
import io.github.rmflawkdyd.luming.presentation.theme.LumingTerracotta
import io.github.rmflawkdyd.luming.presentation.theme.SurfaceWhite

/**
 * 경량 설정 화면 (§2.5). 홈 기어로 진입 — 권한 행(위치·알림) + 앱 버전.
 * 권한 행을 탭하면 시스템 설정 딥링크로 이동(§8.5). 면책문구는 Home에 유지(여기로 이전하지 않음).
 * iOS `SettingsView` 패리티. 디자인: luming.pen `section/PermissionFlow` → `screen/SettingsView`.
 */
@Composable
fun SettingsScreen(
    locationGranted: Boolean,
    notificationGranted: Boolean,
    appVersion: String,
    onBack: () -> Unit,
    onPermissionRowTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeInsets = WindowInsets.safeDrawing.asPaddingValues()
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NavBar(
                onBack = onBack,
                topInset = safeInsets.calculateTopPadding(),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 20.dp,
                        top = 16.dp,
                        end = 20.dp,
                        bottom = safeInsets.calculateBottomPadding() + 20.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SectionLabel(stringResource(R.string.settings_section_permission))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceWhite),
                ) {
                    PermissionRow(
                        icon = Icons.Outlined.LocationOn,
                        iconTint = LumingSlate,
                        iconBackground = LumingSlateBg,
                        title = stringResource(R.string.settings_row_location),
                        granted = locationGranted,
                        onTap = onPermissionRowTap,
                    )
                    HorizontalDivider(thickness = 1.dp, color = LumingPebble)
                    PermissionRow(
                        icon = Icons.Outlined.Notifications,
                        iconTint = LumingClay,
                        iconBackground = LumingClayLight,
                        title = stringResource(R.string.settings_row_notification),
                        granted = notificationGranted,
                        onTap = onPermissionRowTap,
                    )
                }

                Text(
                    text = stringResource(R.string.settings_permission_hint),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = LumingMist,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )

                Spacer(modifier = Modifier.height(14.dp))

                SectionLabel(stringResource(R.string.settings_section_info))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceWhite)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.settings_row_version),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = appVersion,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun NavBar(onBack: () -> Unit, topInset: androidx.compose.ui.unit.Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topInset)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(999.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = stringResource(R.string.cd_back),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.size(44.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

/**
 * 권한 행 (탭 → 시스템 설정 딥링크). OS 권한은 프로그램적으로 변경 불가하므로
 * 토글은 상태 반영용 시각 요소이며, 실제 동작은 행 전체 탭 → SystemSettings.open.
 */
@Composable
private fun PermissionRow(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    granted: Boolean,
    onTap: () -> Unit,
) {
    val statusText = stringResource(
        if (granted) R.string.settings_permission_granted else R.string.settings_permission_denied
    )
    val hint = stringResource(R.string.settings_permission_hint)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .semantics(mergeDescendants = true) { contentDescription = "$title, $statusText. $hint" }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (granted) LumingSage else LumingTerracotta,
            )
        }
        PermissionToggleGlyph(on = granted)
    }
}

/** OS 권한 상태를 반영하는 토글 모양 (실제 토글 아님 — 탭 시 행 전체가 시스템 설정으로 이동). */
@Composable
private fun PermissionToggleGlyph(on: Boolean) {
    Box(
        modifier = Modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (on) LumingSage else LumingPebble)
            .padding(2.dp)
            .clearAndSetSemantics {},
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White),
        )
    }
}
