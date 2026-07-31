//
//  TeeTimeCaddieTheme.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 2/26/24.
//

import SwiftUI
import ThemeUI

// =============================================================================
// Light color scheme — ported 1:1 from Android's LightColorScheme (Theme.kt).
//
// Container deviations from M3 default (which uses tone 90 for all containers):
//   primaryContainer    = fairwayGreen75   deeper green presence on the surface
//   secondaryContainer  = sunGold80        held at 80 — tone 75 muddies the gold
//   tertiaryContainer   = skyBlue75        deeper blue presence
//
// Note: ThemeUI's ThemeColors has no `background`/`onBackground` (Android sets
// these to the same values as surface/onSurface) or `surfaceTint` (= primary on
// Android) slot, so those Android roles are not represented here.
// =============================================================================
fileprivate let ttcLightColors = lightColorScheme(
    primary: fairwayGreen40,
    onPrimary: fairwayGreen100,
    primaryContainer: fairwayGreen75,
    onPrimaryContainer: fairwayGreen10,
    secondary: sunGold40,
    onSecondary: sunGold100,
    secondaryContainer: sunGold80,
    onSecondaryContainer: sunGold10,
    tertiary: skyBlue40,
    onTertiary: skyBlue100,
    tertiaryContainer: skyBlue75,
    onTertiaryContainer: skyBlue10,
    surface: neutral98,
    onSurface: neutral10,
    surfaceVariant: neutralVariant90,
    onSurfaceVariant: neutralVariant30,
    inverseSurface: neutral20,
    inverseOnSurface: neutral95,
    inversePrimary: fairwayGreen80,
    error: errorRed40,
    onError: errorRed100,
    errorContainer: errorRed90,
    onErrorContainer: errorRed10,
    outline: neutralVariant50,
    outlineVariant: neutralVariant80,
    scrim: neutral0,
    surfaceDim: neutral87,
    surfaceBright: neutral98,
    surfaceContainer: neutral94,
    surfaceContainerLowest: neutral100,
    surfaceContainerLow: neutral96,
    surfaceContainerHigh: neutral92,
    surfaceContainerHighest: neutral90
)

// =============================================================================
// Dark color scheme — ported 1:1 from Android's DarkColorScheme (Theme.kt).
//
// Custom dark-scheme surface tone mapping giving more visual separation between
// the canvas, cards, and nav bar:
//   surface / surfaceDim                 : neutral10  (canvas)
//   surfaceContainerLowest               : neutral6
//   surfaceContainerLow / Container      : neutral17  (Cards / NavigationBar)
//   surfaceContainerHigh                 : neutral22
//   surfaceContainerHighest / Bright     : neutral24
// =============================================================================
fileprivate let ttcDarkColors = darkColorScheme(
    primary: fairwayGreen80,
    onPrimary: fairwayGreen20,
    primaryContainer: fairwayGreen30,
    onPrimaryContainer: fairwayGreen90,
    secondary: sunGold80,
    onSecondary: sunGold20,
    secondaryContainer: sunGold30,
    onSecondaryContainer: sunGold90,
    tertiary: skyBlue80,
    onTertiary: skyBlue20,
    tertiaryContainer: skyBlue30,
    onTertiaryContainer: skyBlue90,
    surface: neutral10,
    onSurface: neutral90,
    surfaceVariant: neutralVariant30,
    onSurfaceVariant: neutralVariant80,
    inverseSurface: neutral90,
    inverseOnSurface: neutral20,
    inversePrimary: fairwayGreen40,
    error: errorRed80,
    onError: errorRed20,
    errorContainer: errorRed30,
    onErrorContainer: errorRed90,
    outline: neutralVariant60,
    outlineVariant: neutralVariant30,
    scrim: neutral0,
    surfaceDim: neutral10,
    surfaceBright: neutral24,
    surfaceContainer: neutral17,
    surfaceContainerLowest: neutral6,
    surfaceContainerLow: neutral17,
    surfaceContainerHigh: neutral22,
    surfaceContainerHighest: neutral24
)

fileprivate let ttcTypography = Typography()

fileprivate let ttcShapes = Shapes()

struct TeeTimeCaddieTheme<Content: View>: View {
    private let content: () -> Content

    @Environment(\.colorScheme) var colorMode
    
    private var colors: ThemeColors {
        switch colorMode {
        case .dark:
            ttcDarkColors
        default:
            ttcLightColors
        }
    }
    
    init(
        @ViewBuilder content: @escaping ()->Content) {
            self.content = content
    }
    

    var body: some View {        
        ThemedView(colors: colors, typography: ttcTypography, shapes: ttcShapes, content: content)
    }
}
