package net.bradball.teetimecaddie.android.ui.common.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Light and dark previews of a full screen.
 *
 * `@PreviewLightDark` would be the obvious choice but cannot set `showSystemUi`, which screen
 * previews need to render with the status and navigation bars in place.
 */
@Preview(name = "Light", showBackground = true, showSystemUi = true)
@Preview(
    name = "Dark",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class ScreenPreviews
