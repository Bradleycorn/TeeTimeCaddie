import SwiftUI
import ThemeUI

/// The size a ``TtcChip`` renders at (controls content padding).
enum TtcChipSize {
    case medium
    case small

    /// Fairway Morning chip padding — medium `6×10`, small `4×8`.
    var padding: EdgeInsets {
        switch self {
        case .medium: EdgeInsets(top: 6, leading: 10, bottom: 6, trailing: 10)
        case .small:  EdgeInsets(top: 4, leading: 8, bottom: 4, trailing: 8)
        }
    }
}

/// The surface treatment a ``TtcChip`` renders with: `.filled` (a container fill) or `.outlined`
/// (transparent with a 1pt border). Orthogonal to ``TtcColorRole`` and ``TtcChipSize``.
enum TtcChipVariant {
    case filled
    case outlined
}

/// Role resolution for ``TtcChip``, derived from the Fairway Morning "Chips & status" spec. (The
/// Android twin is `TtcChipDefaults`.)
///
/// Every filled role uses its *container* tone, never the solid one — a chip is a low-emphasis
/// label, so unlike ``TtcButtonStyle`` even `.primary` and `.error` stay soft. `.neutral` sits at
/// `surfaceContainerHigh`, one rung below the button's `surfaceContainerHighest`, because a chip is
/// usually rendered on top of a card.
enum TtcChipStyle {

    /// The chip's clip/border/hit shape — Fairway Morning `radius-sm`, the theme's `small` (8pt). A
    /// rounded rectangle, not a pill; the pill belongs to ``TtcButtonStyle/shape``.
    ///
    /// Takes the scale as a parameter rather than reading the environment, matching
    /// ``filledColors(_:_:)``. That is load-bearing for ``TtcChipButtonStyle``: `@EnvironmentObject`
    /// is not injected into a `ButtonStyle`'s `makeBody`, so ``TtcChip`` resolves the shape and
    /// passes it in.
    ///
    /// The Android twin is `TtcChipDefaults.Shape`.
    static func shape(_ shapes: Shapes) -> AnyShape { shapes.small }

    /// Filled container background + content/foreground colors for `role`.
    static func filledColors(_ role: TtcColorRole, _ scheme: ThemeColors) -> (background: Color, foreground: Color) {
        switch role {
        case .primary:   (scheme.primaryContainer, scheme.onPrimaryContainer)
        case .secondary: (scheme.secondaryContainer, scheme.onSecondaryContainer)
        case .tertiary:  (scheme.tertiaryContainer, scheme.onTertiaryContainer)
        case .error:     (scheme.errorContainer, scheme.onErrorContainer)
        case .neutral:   (scheme.surfaceContainerHigh, scheme.onSurfaceVariant)
        }
    }

    /// Outlined text/content + border colors for `role` (transparent container). `.neutral` is the
    /// design's "suggestion" chip (`onSurfaceVariant` text, `outlineVariant` border); tinted roles
    /// use their role color for both text and border.
    static func outlinedColors(_ role: TtcColorRole, _ scheme: ThemeColors) -> (foreground: Color, border: Color) {
        switch role {
        case .primary:   (scheme.primary, scheme.primary)
        case .secondary: (scheme.secondary, scheme.secondary)
        case .tertiary:  (scheme.tertiary, scheme.tertiary)
        case .error:     (scheme.error, scheme.error)
        case .neutral:   (scheme.onSurfaceVariant, scheme.outlineVariant)
        }
    }
}

/// The shared chip "chrome" for the Fairway Morning chips: a leading-icon + label row on an
/// `radius-sm`-rounded fill, with an optional 1pt border (used by the `.outlined` ``TtcChip`` variant).
///
/// The 14pt icon and `caption`-weight label inherit the chip's [foreground] color. Colors, border and
/// [shape] are resolved by the caller from the theme and passed in (like `TtcButtonLabel`), so the
/// same chrome serves both the filled and outlined chips, interactive or static.
struct TtcChipLabel: View {
    let text: String
    let icon: ImageResource?
    let size: TtcChipSize
    let background: Color
    let foreground: Color
    /// Border color, or `nil` for a borderless (filled) chip.
    let border: Color?
    /// The chip's clip/border shape — the theme's `small`, resolved by ``TtcChip``.
    let shape: AnyShape

    var body: some View {
        HStack(spacing: 6) {
            if let icon {
                Image(icon)
                    .font(.system(size: 14))
            }
            Text(text)
                .font(.caption.weight(.medium))
        }
        .padding(size.padding)
        .background(background)
        .foregroundStyle(foreground)
        .overlay {
            if let border {
                // `strokeBorder` needs an `InsettableShape`, and the theme's shapes are type-erased
                // to `AnyShape`. `stroke` centers the line on the path instead, so draw it at double
                // width and let the trailing `clipShape` remove the outer half — the same 1pt inset
                // border `strokeBorder(_, lineWidth: 1)` produced.
                shape.stroke(border, lineWidth: 2)
            }
        }
        .clipShape(shape)
    }
}

/// The `ButtonStyle` used when a chip is tappable (`onClick != nil`). The chip chrome lives in
/// ``TtcChipLabel``; this only adds the press feedback + hit shape.
struct TtcChipButtonStyle: ButtonStyle {
    /// The chip's hit shape, resolved from the theme by ``TtcChip`` and passed in — a `ButtonStyle`
    /// cannot read `@EnvironmentObject` in `makeBody`.
    let shape: AnyShape

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .contentShape(shape)
            .opacity(configuration.isPressed ? 0.85 : 1)
    }
}
