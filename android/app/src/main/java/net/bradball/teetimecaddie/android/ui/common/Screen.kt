package net.bradball.teetimecaddie.android.ui.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import net.bradball.teetimecaddie.android.analytics.LocalEventManager
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen

/**
 * Wrapper for rendering a "Screen" of content.
 * This composable handles common "screen level" things,
 * including using the [LocalEventManager] to log a "screen view"
 * for the composed content.
 *
 * @param name The name of the screen. Used to log the screen view.
 * @param extras a Map of additional key/vale pairs to log with the screen view.
 * @param content a composable function that makes up the content of this screen.
 */
@Composable
fun Screen(
    screen: AnalyticsScreen,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    content: @Composable ()->Unit) {

    val eventManager = LocalEventManager.current
    LaunchedEffect(screen) {
        eventManager.logScreenView(screen)
    }

    Surface(modifier, color = color, contentColor = contentColor) {
        content()
    }
}