import SwiftUI
import ThemeUI

/// The color role a ``TtcChip`` renders with.
///
/// A `.filled` ``TtcChip`` uses the role's *container* tones (softer than a ``TtcButton``): `.primary`
/// is the green "organizer" chip, `.secondary` the gold "pending" chip, `.tertiary` the sky "confirmed"
/// chip, and `.neutral` the ambient surface treatment. An `.outlined` ``TtcChip`` tints its text +
/// border to the role, with `.neutral` using the subtle `onSurfaceVariant` / `outlineVariant`
/// "suggestion" treatment.
///
/// Resolves to the Fairway Morning design system roles via the app's `AppTheme` `ThemeColors`.
enum TtcChipColor {
    case primary
    case secondary
    case tertiary
    case neutral
}

// MARK: - Role resolution

extension TtcChipColor {

    /// Filled container background + content/foreground colors for this role. Filled chips use the
    /// role's *container* tone; `.neutral` uses `surfaceContainerHigh` / `onSurfaceVariant`.
    func filledColors(_ scheme: ThemeColors) -> (background: Color, foreground: Color) {
        switch self {
        case .primary:   (scheme.primaryContainer, scheme.onPrimaryContainer)
        case .secondary: (scheme.secondaryContainer, scheme.onSecondaryContainer)
        case .tertiary:  (scheme.tertiaryContainer, scheme.onTertiaryContainer)
        case .neutral:   (scheme.surfaceContainerHigh, scheme.onSurfaceVariant)
        }
    }

    /// Outlined text/content + border colors for this role (transparent container). `.neutral` is the
    /// design's "suggestion" chip (`onSurfaceVariant` text, `outlineVariant` border); tinted roles use
    /// their role color for both text and border.
    func outlinedColors(_ scheme: ThemeColors) -> (foreground: Color, border: Color) {
        switch self {
        case .primary:   (scheme.primary, scheme.primary)
        case .secondary: (scheme.secondary, scheme.secondary)
        case .tertiary:  (scheme.tertiary, scheme.tertiary)
        case .neutral:   (scheme.onSurfaceVariant, scheme.outlineVariant)
        }
    }
}
