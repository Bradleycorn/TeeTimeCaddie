package net.bradball.teetimecaddie.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.bradball.teetimecaddie.android.analytics.LocalEventManager
import net.bradball.teetimecaddie.android.initializers.InitializationState
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.app.TeeTimeCaddieApp
import net.bradball.teetimecaddie.android.ui.app.rememberTeeTimeCaddieAppState
import net.bradball.teetimecaddie.android.ui.common.AnimatedLoadingScrim

@AndroidEntryPoint
class TeeTimeCaddieActivity : ComponentActivity() {

    private val viewModel: TeeTimeCaddieActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashscreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var showSplashScreen: Boolean by mutableStateOf(true)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.showSplashScreen.onEach { showSplashScreen = it }.collect()
            }
        }

        splashscreen.setKeepOnScreenCondition { showSplashScreen }

        // Turn off the decor fitting system windows, which allows us to handle insets,
        // including IME animations
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val showApp by viewModel.showApp.collectAsStateWithLifecycle()
            val appState = rememberTeeTimeCaddieAppState(appInitializers = viewModel.appInitializers, authRepository = viewModel.authRepo)
            val systemUiController = rememberSystemUiController()

            DisposableEffect(key1 = systemUiController) {
                systemUiController.systemBarsDarkContentEnabled = true
                onDispose { /* noop */ }
            }

            CompositionLocalProvider(LocalEventManager provides viewModel.eventManager) {
                MyApplicationTheme(darkTheme = isSystemInDarkTheme()) {
                    if (showApp) {
                        TeeTimeCaddieApp(appState)
                    }
                }
            }
        }
    }
}