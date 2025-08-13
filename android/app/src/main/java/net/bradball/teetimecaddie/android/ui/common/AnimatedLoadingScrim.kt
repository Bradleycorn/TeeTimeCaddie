package net.bradball.teetimecaddie.android.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme

/**
 * Displays an animated loading scrim with a circular progress indicator.
 *
 * This composable creates a full-screen overlay (scrim) with a centered circular progress indicator.
 * The scrim animates in and out of view based on the [isVisible] parameter.
 *
 * @param isVisible Boolean flag to control the visibility of the loading scrim.
 *                  When true, the scrim is displayed; when false, it's hidden.
 *                  Defaults to true.
 *
 *
 * The scrim uses the following animations:
 * - Enter: Slides in vertically from the middle of the screen while fading in.
 * - Exit: Slides out vertically towards the middle of the screen while fading out.
 *
 * The scrim's appearance is determined by the current [MaterialTheme]:
 * - Background color: [MaterialTheme.colorScheme.scrim]
 * - Content color: [MaterialTheme.colorScheme.onSurface]
 *
 * The circular progress indicator is sized at 64.dp.
 *
 * Usage:
 * ```
 * AnimatedLoadingScrim(isVisible = viewModel.isLoading)
 * ```
 *
 * @see AnimatedVisibility
 * @see CircularProgressIndicator
 * @see MaterialTheme
 */
@Composable
fun AnimatedLoadingScrim(isVisible: Boolean = true) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it/2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it/2 }) + fadeOut()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.scrim,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.requiredSize(64.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun PreviewFullScreenLoadingSpinner() {
    MyApplicationTheme {
        AnimatedLoadingScrim()
    }
}