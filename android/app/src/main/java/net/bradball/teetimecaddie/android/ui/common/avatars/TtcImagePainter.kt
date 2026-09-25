package net.bradball.teetimecaddie.android.ui.common.avatars

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter

/**
 * Loads [model] and returns a [Painter] for it, or `null` until it has actually loaded.
 *
 * This is the **only** place in the app that touches Coil. Every avatar component takes a plain
 * [Painter], so it can be handed a `ColorPainter` in a preview and a network image at runtime
 * without knowing the difference — and previews never reach the network.
 *
 * Returning `null` rather than the painter in a loading or error state is what lets a caller fall
 * back to the initial-letter placeholder. A Coil painter in its error state paints nothing, which
 * would leave a hole where a broken photo URL ought to degrade to an initial.
 *
 * @param model Anything Coil accepts — typically a photo URL, or `null` for no photo at all.
 * @return The loaded image, or `null` while loading, on failure, or when [model] is `null`.
 */
@Composable
fun rememberTtcImagePainter(model: Any?): Painter? {
    if (model == null) return null

    val painter = rememberAsyncImagePainter(model)
    val state by painter.state.collectAsStateWithLifecycle()

    return if (state is AsyncImagePainter.State.Success) painter else null
}
