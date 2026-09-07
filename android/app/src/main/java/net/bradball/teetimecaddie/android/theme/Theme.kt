package net.bradball.teetimecaddie.android.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// =============================================================================
// Light color scheme
//
// Container deviations from M3 default (which uses tone 90 for all containers):
//   primaryContainer    = fairwayGreen75   deeper green presence on the surface
//   secondaryContainer  = sunGold80        held at 80 — tone 75 muddies the gold
//   tertiaryContainer   = skyBlue75        deeper blue presence
//
// All other roles follow standard M3 light-scheme tone mapping.
// =============================================================================
val LightColorScheme: ColorScheme = lightColorScheme(
    primary               = fairwayGreen40,
    onPrimary             = fairwayGreen100,
    primaryContainer      = fairwayGreen75,
    onPrimaryContainer    = fairwayGreen10,
    inversePrimary        = fairwayGreen80,
    secondary             = sunGold40,
    onSecondary           = sunGold100,
    secondaryContainer    = sunGold80,
    onSecondaryContainer  = sunGold10,
    tertiary              = skyBlue40,
    onTertiary            = skyBlue100,
    tertiaryContainer     = skyBlue75,
    onTertiaryContainer   = skyBlue10,
    background            = neutral98,
    onBackground          = neutral10,
    surface               = neutral98,
    onSurface             = neutral10,
    surfaceVariant        = neutralVariant90,
    onSurfaceVariant      = neutralVariant30,
    surfaceTint           = fairwayGreen40,
    inverseSurface        = neutral20,
    inverseOnSurface      = neutral95,
    error                 = errorRed40,
    onError               = errorRed100,
    errorContainer        = errorRed90,
    onErrorContainer      = errorRed10,
    outline               = neutralVariant50,
    outlineVariant        = neutralVariant80,
    scrim                 = neutral0,
    surfaceBright         = neutral98,
    surfaceDim            = neutral87,
    surfaceContainerLowest  = neutral100,
    surfaceContainerLow   = neutral96,
    surfaceContainer      = neutral94,
    surfaceContainerHigh  = neutral92,
    surfaceContainerHighest = neutral90,
)

// =============================================================================
// Dark color scheme
//
// Custom dark-scheme surface tone mapping. Deviates from M3 canonical (which
// puts surface at tone 6 with a compressed container ramp) to give more
// visual separation between the canvas, cards, and nav bar. Colored role
// containers still use standard tone 30.
//
// Surface ramp used here:
//   surface / background / surfaceDim    : neutral10  (canvas)
//   surfaceContainerLowest               : neutral6
//   surfaceContainerLow                  : neutral17  (Cards land here)
//   surfaceContainer                     : neutral17  (NavigationBar lands here)
//   surfaceContainerHigh                 : neutral22
//   surfaceContainerHighest              : neutral24
//   surfaceBright                        : neutral24
// =============================================================================
val DarkColorScheme: ColorScheme = darkColorScheme(
    primary               = fairwayGreen80,
    onPrimary             = fairwayGreen20,
    primaryContainer      = fairwayGreen30,
    onPrimaryContainer    = fairwayGreen90,
    inversePrimary        = fairwayGreen40,
    secondary             = sunGold80,
    onSecondary           = sunGold20,
    secondaryContainer    = sunGold30,
    onSecondaryContainer  = sunGold90,
    tertiary              = skyBlue80,
    onTertiary            = skyBlue20,
    tertiaryContainer     = skyBlue30,
    onTertiaryContainer   = skyBlue90,
    background            = neutral10,
    onBackground          = neutral90,
    surface               = neutral10,
    onSurface             = neutral90,
    surfaceVariant        = neutralVariant30,
    onSurfaceVariant      = neutralVariant80,
    surfaceTint           = fairwayGreen80,
    inverseSurface        = neutral90,
    inverseOnSurface      = neutral20,
    error                 = errorRed80,
    onError               = errorRed20,
    errorContainer        = errorRed30,
    onErrorContainer      = errorRed90,
    outline               = neutralVariant60,
    outlineVariant        = neutralVariant30,
    scrim                 = neutral0,
    surfaceBright         = neutral24,
    surfaceDim            = neutral10,
    surfaceContainerLowest  = neutral6,
    surfaceContainerLow   = neutral17,
    surfaceContainer      = neutral17,
    surfaceContainerHigh  = neutral22,
    surfaceContainerHighest = neutral24,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = TtcShapes,
        content = content
    )
}