import SwiftUI
import ThemeUI

/// Shared sizing and geometry values for ``TtcAvatar``, ``TtcPlayerStack`` and ``TtcPhotoPicker``,
/// derived from the Fairway Morning "Avatars & players" spec. (Parallels `TtcChipStyle`.)
///
/// The glyphs use SF Symbols (`person.fill`, `plus`, `camera.fill`, `pencil`) rather than asset
/// symbolsets — they're standard system icons, so no custom art is needed.
///
/// An initials avatar is an identity marker, so it takes the **solid** role tone the way a filled
/// button does — with the same exception for `.secondary`, which falls back to `secondaryContainer`
/// because the gold at full strength cannot carry legible initials.
/// ``TtcButtonStyle/filledColors(_:_:)`` makes the same substitution for the same reason; if the
/// gold changes, both need revisiting.
enum TtcAvatarStyle {

    /// Named diameters from the design specimens. `size` on the components is a free `CGFloat`.
    static let smallSize: CGFloat = 28
    static let mediumSize: CGFloat = 40
    static let largeSize: CGFloat = 56

    /// Diameter of the ``TtcPhotoPicker`` circle.
    static let pickerSize: CGFloat = 88

    /// Horizontal overlap between adjacent avatars in a ``TtcPlayerStack``.
    static let stackOverlap: CGFloat = 10

    /// Width of the ``TtcPlayerItem`` separator ring drawn around each stacked avatar.
    static let stackRing: CGFloat = 2

    /// Stroke width of the guest/empty placeholder ring.
    static let borderWidth: CGFloat = 1.5

    /// Dashed stroke used for the guest/empty placeholder ring and the picker's empty state.
    static func dashedStroke() -> StrokeStyle {
        StrokeStyle(lineWidth: borderWidth, dash: [4, 3])
    }

    /// Initials point size — ~40% of the avatar diameter.
    static func textSize(_ size: CGFloat) -> CGFloat { size * 0.4 }

    /// Placeholder glyph point size — ~half the avatar diameter.
    static func iconSize(_ size: CGFloat) -> CGFloat { size * 0.5 }

    /// Background + content colors for an initials ``TtcAvatar`` in the given `role`.
    ///
    /// `.primary`/`.tertiary` use the solid role; `.secondary` uses the container tone (see the type
    /// doc). `.error` marks a player with a conflict — a declined invite, a scheduling clash — and
    /// `.neutral` an inactive or unrecognized player who still has a name to show. For a player with
    /// no name at all, use a ``TtcAvatarPlaceholder`` instead.
    static func toneColors(_ role: TtcColorRole, _ scheme: ThemeColors) -> (background: Color, foreground: Color) {
        switch role {
        case .primary:   (scheme.primary, scheme.onPrimary)
        case .secondary: (scheme.secondaryContainer, scheme.onSecondaryContainer)
        case .tertiary:  (scheme.tertiary, scheme.onTertiary)
        case .error:     (scheme.errorContainer, scheme.onErrorContainer)
        case .neutral:   (scheme.surfaceContainerHighest, scheme.onSurfaceVariant)
        }
    }

    /// SF Symbol name for a placeholder glyph.
    static func placeholderSymbol(_ placeholder: TtcAvatarPlaceholder) -> String {
        switch placeholder {
        case .guest: "person.fill"
        case .empty: "plus"
        }
    }
}
