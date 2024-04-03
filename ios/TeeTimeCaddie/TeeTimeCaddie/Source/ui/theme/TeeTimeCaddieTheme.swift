//
//  TeeTimeCaddieTheme.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 2/26/24.
//

import SwiftUI
import ThemeUI

fileprivate let ttcLightColors = lightColorScheme(
    primary: Purple600,
    onPrimary: .white,
    primaryContainer: Color(.white500),
    onPrimaryContainer: Color(.purple100),
    inversePrimary: Color(.purple800),
    secondary: Color(.purpleGray500),
    onSecondary: .white,
    secondaryContainer: Color(.white800),
    onSecondaryContainer: Color(.purpleGray100),
    tertiary: Color(.plum600),
    onTertiary: .white,
    tertiaryContainer: Color(.white600),
    onTertiaryContainer: Color(.plum200),
    background: Color(.white900),
    onBackground: Color(.gray100),
    surface: Color(.white900),
    onSurface: Color(.gray100),
    surfaceVariant: Color(.white400),
    onSurfaceVariant: Color(.gray300),
    surfaceTint: Color(.purple600),
    inverseSurface: Color(.gray200),
    inverseOnSurface: Color(.white700),
    error: Color(.red700),
    onError: .white,
    errorContainer: Color(.white200),
    onErrorContainer: Color(.red200),
    outline: Color(.gray500),
    outlineVariant: Color(.gray900),
    scrim: .black
)

//TODO: Define color palette for dark mode
fileprivate let ttcDarkColors = darkColorScheme()

fileprivate let ttcTypography = Typography()

fileprivate let ttcShapes = Shapes()

struct TeeTimeCaddieTheme<Content: View>: View {
    private let content: () -> Content

    @Environment(\.colorScheme) var colorMode
    
    private var colors: Colors {
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
