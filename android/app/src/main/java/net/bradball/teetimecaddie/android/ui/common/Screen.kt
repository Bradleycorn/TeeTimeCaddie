package net.bradball.teetimecaddie.android.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import net.bradball.teetimecaddie.android.analytics.LocalEventManager

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
    name: String,
    extras: Map<String, String> = emptyMap(),
    content: @Composable ()->Unit) {

    val eventManager = LocalEventManager.current
    LaunchedEffect(name, extras) {
        eventManager.logScreenView(name, extras)
    }

    content()
}