package io.github.rmflawkdyd.luming

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import io.github.rmflawkdyd.luming.presentation.navigation.LumingNavGraph
import io.github.rmflawkdyd.luming.presentation.theme.ColorIntBackgroundWarm
import io.github.rmflawkdyd.luming.presentation.theme.ColorIntOnSurface
import io.github.rmflawkdyd.luming.presentation.theme.LumingTheme
import androidx.activity.viewModels
import io.github.rmflawkdyd.luming.presentation.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(ColorIntBackgroundWarm, ColorIntOnSurface),
        )
        setContent {
            LumingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    LumingNavGraph(navController = navController)
                }
            }
        }
    }

}
