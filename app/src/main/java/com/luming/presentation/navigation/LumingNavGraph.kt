package com.luming.presentation.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.luming.presentation.MainViewModel
import com.luming.presentation.home.HomeScreen
import com.luming.presentation.home.HomeViewModel
import com.luming.presentation.instruction.InstructionScreen
import com.luming.presentation.instruction.InstructionViewModel
import com.luming.presentation.permission.PermissionScreen
import com.luming.presentation.settings.SettingsScreen
import com.luming.presentation.settings.SettingsViewModel
import com.luming.presentation.util.SystemSettings

private object Screen {
    const val PERMISSION = "permission"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val INSTRUCTION = "instruction/{activityId}"
    fun instruction(activityId: String) = "instruction/$activityId"
}

@Composable
fun LumingNavGraph(navController: NavHostController) {
    val mainViewModel: MainViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = if (mainViewModel.needsPermission) Screen.PERMISSION else Screen.HOME,
    ) {

        composable(Screen.PERMISSION) {
            PermissionScreen(
                onPermissionsHandled = { notificationGranted ->
                    mainViewModel.onPermissionsResult(notificationGranted)
                    navController.navigate(Screen.HOME) {
                        popUpTo(Screen.PERMISSION) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.HOME) { entry ->
            val vm: HomeViewModel = hiltViewModel()
            val uiState by vm.uiState.collectAsStateWithLifecycle()
            val locationDenied by vm.locationDenied.collectAsStateWithLifecycle()

            val context = LocalContext.current
            val activity = context as? ComponentActivity
            BackHandler { activity?.finish() }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) vm.onResume()
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            val activityCompleted by entry.savedStateHandle
                .getStateFlow("activity_completed", false)
                .collectAsStateWithLifecycle()

            LaunchedEffect(activityCompleted) {
                if (activityCompleted) {
                    vm.showCompletionOverlay()
                    entry.savedStateHandle["activity_completed"] = false
                }
            }

            HomeScreen(
                uiState = uiState,
                locationDenied = locationDenied,
                onActivityClick = { activityId ->
                    navController.navigate(Screen.instruction(activityId))
                },
                onRefresh = vm::refresh,
                onOverlayDismissed = vm::onCompletionOverlayDismissed,
                onLocationBannerTap = { SystemSettings.open(context) },
                onSettingsClick = { navController.navigate(Screen.SETTINGS) },
            )
        }

        composable(Screen.SETTINGS) {
            val vm: SettingsViewModel = hiltViewModel()
            val locationGranted by vm.locationGranted.collectAsStateWithLifecycle()
            val notificationGranted by vm.notificationGranted.collectAsStateWithLifecycle()

            val context = LocalContext.current

            // 시스템 설정에서 권한을 바꾸고 돌아오면(ON_RESUME) 라이브 상태를 다시 읽는다 (AC-25/AC-26).
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) vm.refresh()
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            SettingsScreen(
                locationGranted = locationGranted,
                notificationGranted = notificationGranted,
                appVersion = vm.appVersion,
                onBack = { navController.popBackStack() },
                onPermissionRowTap = { SystemSettings.open(context) },
            )
        }

        composable(
            route = Screen.INSTRUCTION,
            arguments = listOf(navArgument("activityId") { type = NavType.StringType }),
        ) {
            val vm: InstructionViewModel = hiltViewModel()
            val uiState by vm.uiState.collectAsStateWithLifecycle()

            fun navigateComplete() {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("activity_completed", true)
                navController.popBackStack()
            }

            // 이탈(aborting) 내비게이션은 상태 이벤트로 처리 — 다이얼로그가 먼저 닫힌 뒤 전환되도록 한다.
            LaunchedEffect(uiState?.navigateBack) {
                if (uiState?.navigateBack == true) navController.popBackStack()
            }

            InstructionScreen(
                uiState = uiState,
                onNext = vm::goToNextStep,
                onPrevious = vm::goToPreviousStep,
                onStart = vm::startTimer,
                onComplete = { elapsedMs -> vm.requestComplete(elapsedMs) { navigateComplete() } },
                onConfirmWarning = { vm.confirmComplete { navigateComplete() } },
                onDismissWarning = vm::dismissWarning,
                onBack = vm::requestAbort,
                onConfirmAbort = vm::confirmAbort,
                onDismissAbort = vm::dismissAbort,
            )
        }
    }
}
