package io.github.rmflawkdyd.luming.presentation.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun PermissionScreen(
    onPermissionsHandled: (notificationGranted: Boolean) -> Unit,
) {
    val context = LocalContext.current

    val permissionsToRequest = remember {
        buildList {
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            results[Manifest.permission.POST_NOTIFICATIONS] == true
        } else true
        onPermissionsHandled(notifGranted)
    }

    LaunchedEffect(Unit) {
        val allGranted = permissionsToRequest.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) onPermissionsHandled(true)
        else permissionLauncher.launch(permissionsToRequest)
    }

    Box(modifier = Modifier.fillMaxSize())
}
