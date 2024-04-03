//
//  Color.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 7/19/23.
//

import Foundation
import SwiftUI

public class Colors: ObservableObject {
    @Published public private(set) var primary: Color
    @Published public private(set) var onPrimary: Color
    @Published public private(set) var primaryContainer: Color
    @Published public private(set) var onPrimaryContainer: Color
    @Published public private(set) var inversePrimary: Color
    @Published public private(set) var secondary: Color
    @Published public private(set) var onSecondary: Color
    @Published public private(set) var secondaryContainer: Color
    @Published public private(set) var onSecondaryContainer: Color
    @Published public private(set) var tertiary: Color
    @Published public private(set) var onTertiary: Color
    @Published public private(set) var tertiaryContainer: Color
    @Published public private(set) var onTertiaryContainer: Color
    @Published public private(set) var background: Color
    @Published public private(set) var onBackground: Color
    @Published public private(set) var surface: Color
    @Published public private(set) var onSurface: Color
    @Published public private(set) var surfaceVariant: Color
    @Published public private(set) var onSurfaceVariant: Color
    @Published public private(set) var surfaceTint: Color
    @Published public private(set) var inverseSurface: Color
    @Published public private(set) var inverseOnSurface: Color
    @Published public private(set) var error: Color
    @Published public private(set) var onError: Color
    @Published public private(set) var errorContainer: Color
    @Published public private(set) var onErrorContainer: Color
    @Published public private(set) var outline: Color
    @Published public private(set) var outlineVariant: Color
    @Published public private(set) var scrim: Color
    
    public init(
        primary: Color,
        onPrimary: Color,
        primaryContainer: Color,
        onPrimaryContainer: Color,
        inversePrimary: Color,
        secondary: Color,
        onSecondary: Color,
        secondaryContainer: Color,
        onSecondaryContainer: Color,
        tertiary: Color,
        onTertiary: Color,
        tertiaryContainer: Color,
        onTertiaryContainer: Color,
        background: Color,
        onBackground: Color,
        surface: Color,
        onSurface: Color,
        surfaceVariant: Color,
        onSurfaceVariant: Color,
        surfaceTint: Color,
        inverseSurface: Color,
        inverseOnSurface: Color,
        error: Color,
        onError: Color,
        errorContainer: Color,
        onErrorContainer: Color,
        outline: Color,
        outlineVariant: Color,
        scrim: Color
    ) {
        self.primary = primary
        self.onPrimary = onPrimary
        self.primaryContainer = primaryContainer
        self.onPrimaryContainer = onPrimaryContainer
        self.inversePrimary = inversePrimary
        self.secondary = secondary
        self.onSecondary = onSecondary
        self.secondaryContainer = secondaryContainer
        self.onSecondaryContainer = onSecondaryContainer
        self.tertiary = tertiary
        self.onTertiary = onTertiary
        self.tertiaryContainer = tertiaryContainer
        self.onTertiaryContainer = onTertiaryContainer
        self.background = background
        self.onBackground = onBackground
        self.surface = surface
        self.onSurface = onSurface
        self.surfaceVariant = surfaceVariant
        self.onSurfaceVariant = onSurfaceVariant
        self.surfaceTint = surfaceTint
        self.inverseSurface = inverseSurface
        self.inverseOnSurface = inverseOnSurface
        self.error = error
        self.onError = onError
        self.errorContainer = errorContainer
        self.onErrorContainer = onErrorContainer
        self.outline = outline
        self.outlineVariant = outlineVariant
        self.scrim = scrim
    }
    
    public func contentColorFor(_ color: Color) -> Color {
        switch color {
        case primary:            onPrimary
        case primaryContainer:   onPrimaryContainer
        case onPrimaryContainer: primaryContainer
        case secondary:          onSecondary
        case secondaryContainer: onSecondaryContainer
        case tertiary:           onTertiary
        case tertiaryContainer:  onTertiaryContainer
        case background:         onBackground
        case surface:            onSurface
        case surfaceVariant:     onSurfaceVariant
        case inverseSurface:     inverseOnSurface
        case error:              onError
        case errorContainer:     onErrorContainer
        default:
            Color.clear
        }
    }

    
}

public func lightColorScheme(
    primary: Color = default_light_primary,
    onPrimary: Color = default_light_onPrimary,
    primaryContainer: Color = default_light_primaryContainer,
    onPrimaryContainer: Color = default_light_onPrimaryContainer,
    inversePrimary: Color = default_light_inversePrimary,
    secondary: Color = default_light_secondary,
    onSecondary: Color = default_light_onSecondary,
    secondaryContainer: Color = default_light_secondaryContainer,
    onSecondaryContainer: Color = default_light_onSecondaryContainer,
    tertiary: Color = default_light_tertiary,
    onTertiary: Color = default_light_onTertiary,
    tertiaryContainer: Color = default_light_tertiaryContainer,
    onTertiaryContainer: Color = default_light_onTertiaryContainer,
    background: Color = default_light_background,
    onBackground: Color = default_light_onBackground,
    surface: Color = default_light_surface,
    onSurface: Color = default_light_onSurface,
    surfaceVariant: Color = default_light_surfaceVariant,
    onSurfaceVariant: Color = default_light_onSurfaceVariant,
    surfaceTint: Color = default_light_surfaceTint,
    inverseSurface: Color = default_light_inverseSurface,
    inverseOnSurface: Color = default_light_inverseOnSurface,
    error: Color = default_light_error,
    onError: Color = default_light_onError,
    errorContainer: Color = default_light_errorContainer,
    onErrorContainer: Color = default_light_onErrorContainer,
    outline: Color = default_light_outline,
    outlineVariant: Color = default_light_outlineVariant,
    scrim: Color = default_light_scrim
) -> Colors {
    return Colors(
        primary: primary,
        onPrimary: onPrimary,
        primaryContainer: primaryContainer,
        onPrimaryContainer: onPrimaryContainer,
        inversePrimary: inversePrimary,
        secondary: secondary,
        onSecondary: onSecondary,
        secondaryContainer: secondaryContainer,
        onSecondaryContainer: onSecondaryContainer,
        tertiary: tertiary,
        onTertiary: onTertiary,
        tertiaryContainer: tertiaryContainer,
        onTertiaryContainer: onTertiaryContainer,
        background: background,
        onBackground: onBackground,
        surface: surface,
        onSurface: onSurface,
        surfaceVariant: surfaceVariant,
        onSurfaceVariant: onSurfaceVariant,
        surfaceTint: surfaceTint,
        inverseSurface: inverseSurface,
        inverseOnSurface: inverseOnSurface,
        error: error,
        onError: onError,
        errorContainer: errorContainer,
        onErrorContainer: onErrorContainer,
        outline: outline,
        outlineVariant: outlineVariant,
        scrim: scrim)
}

public func darkColorScheme(
    primary: Color = default_dark_primary,
    onPrimary: Color = default_dark_onPrimary,
    primaryContainer: Color = default_dark_primaryContainer,
    onPrimaryContainer: Color = default_dark_onPrimaryContainer,
    inversePrimary: Color = default_dark_inversePrimary,
    secondary: Color = default_dark_secondary,
    onSecondary: Color = default_dark_onSecondary,
    secondaryContainer: Color = default_dark_secondaryContainer,
    onSecondaryContainer: Color = default_dark_onSecondaryContainer,
    tertiary: Color = default_dark_tertiary,
    onTertiary: Color = default_dark_onTertiary,
    tertiaryContainer: Color = default_dark_tertiaryContainer,
    onTertiaryContainer: Color = default_dark_onTertiaryContainer,
    background: Color = default_dark_background,
    onBackground: Color = default_dark_onBackground,
    surface: Color = default_dark_surface,
    onSurface: Color = default_dark_onSurface,
    surfaceVariant: Color = default_dark_surfaceVariant,
    onSurfaceVariant: Color = default_dark_onSurfaceVariant,
    surfaceTint: Color = default_dark_surfaceTint,
    inverseSurface: Color = default_dark_inverseSurface,
    inverseOnSurface: Color = default_dark_inverseOnSurface,
    error: Color = default_dark_error,
    onError: Color = default_dark_onError,
    errorContainer: Color = default_dark_errorContainer,
    onErrorContainer: Color = default_dark_onErrorContainer,
    outline: Color = default_dark_outline,
    outlineVariant: Color = default_dark_outlineVariant,
    scrim: Color = default_dark_scrim
) -> Colors {
    return Colors(
        primary: primary,
        onPrimary: onPrimary,
        primaryContainer: primaryContainer,
        onPrimaryContainer: onPrimaryContainer,
        inversePrimary: inversePrimary,
        secondary: secondary,
        onSecondary: onSecondary,
        secondaryContainer: secondaryContainer,
        onSecondaryContainer: onSecondaryContainer,
        tertiary: tertiary,
        onTertiary: onTertiary,
        tertiaryContainer: tertiaryContainer,
        onTertiaryContainer: onTertiaryContainer,
        background: background,
        onBackground: onBackground,
        surface: surface,
        onSurface: onSurface,
        surfaceVariant: surfaceVariant,
        onSurfaceVariant: onSurfaceVariant,
        surfaceTint: surfaceTint,
        inverseSurface: inverseSurface,
        inverseOnSurface: inverseOnSurface,
        error: error,
        onError: onError,
        errorContainer: errorContainer,
        onErrorContainer: onErrorContainer,
        outline: outline,
        outlineVariant: outlineVariant,
        scrim: scrim)
}

fileprivate func lightModeContrastColor(for color: Color) -> Color {
    let luminanceThreshold = 0.175
    if (color.luminance() > luminanceThreshold) {
        return Color.black
    } else {
        return Color.white
    }
}

fileprivate func darkModeContrastColor(for color: Color) -> Color {
    let luminanceThreshold = 0.1833333
    if (color.luminance() > luminanceThreshold) {
        return Color.black
    } else {
        return Color.white
    }
}
