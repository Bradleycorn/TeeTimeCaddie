import SwiftUI
import ThemeUI

/// The color role a ``TtcButton`` or ``TtcOutlinedButton`` renders with.
///
/// For a filled ``TtcButton``, `.primary` is the solid green CTA while `.secondary` and `.tertiary`
/// produce the "tonal" look (a container fill). For a ``TtcOutlinedButton``, `.neutral` is the default
/// design treatment (on-surface text, outline border) and the other roles tint the text and border.
///
/// Resolves to the Fairway Morning design system roles via the app's `AppTheme` `ThemeColors`.
enum TtcButtonColor {
    case primary
    case secondary
    case tertiary
    case neutral
}

// MARK: - Role resolution

extension TtcButtonColor {

    /// Filled container background + content/foreground colors for this role.
    func filledColors(_ scheme: ThemeColors) -> (background: Color, foreground: Color) {
        switch self {
        case .primary:   (scheme.primary, scheme.onPrimary)
        case .secondary: (scheme.secondaryContainer, scheme.onSecondaryContainer)
        case .tertiary:  (scheme.tertiaryContainer, scheme.onTertiaryContainer)
        case .neutral:   (scheme.surfaceContainerHighest, scheme.onSurfaceVariant)
        }
    }

    /// Outlined text/content color. `.neutral` (the design default) uses `onSurface`;
    /// the other roles tint to that role color.
    func outlinedForeground(_ scheme: ThemeColors) -> Color {
        switch self {
        case .neutral:   scheme.onSurface
        case .primary:   scheme.primary
        case .secondary: scheme.secondary
        case .tertiary:  scheme.tertiary
        }
    }

    /// Outlined border color. `.neutral` uses the subtle `outline` role (distinct from the text
    /// color, per the design); tinted roles use their role color for the border.
    func outlinedBorder(_ scheme: ThemeColors) -> Color {
        switch self {
        case .neutral: scheme.outline
        default:       outlinedForeground(scheme)
        }
    }
}
