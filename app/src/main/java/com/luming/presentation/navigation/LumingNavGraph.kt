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
import com.luming.presentation.home.HomeScreen
import com.luming.presentation.home.HomeViewModel
import com.luming.presentation.instruction.InstructionScreen
import com.luming.presentation.instruction.InstructionViewModel

private object Screen {
    const val HOME = "home"
    const val INSTRUCTION = "instruction/{activityId}"
    fun instruction(activityId: String) = "instruction/$activityId"
}

@Composable
fun LumingNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.HOME) {

        composable(Screen.HOME) { entry ->
            val vm: HomeViewModel = hiltViewModel()
            val uiState by vm.uiState.collectAsStateWithLifecycle()

            val activity = LocalContext.current as? ComponentActivity
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
                onActivityClick = { activityId ->
                    navController.navigate(Screen.instruction(activityId))
                },
                onRefresh = vm::refresh,
                onOverlayDismissed = vm::onCompletionOverlayDismissed,
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

            InstructionScreen(
                uiState = uiState,
                onNext = vm::goToNextStep,
                onPrevious = vm::goToPreviousStep,
                onStart = vm::startTimer,
                onComplete = { elapsedMs -> vm.requestComplete(elapsedMs) { navigateComplete() } },
                onConfirmWarning = { vm.confirmComplete { navigateComplete() } },
                onDismissWarning = vm::dismissWarning,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
