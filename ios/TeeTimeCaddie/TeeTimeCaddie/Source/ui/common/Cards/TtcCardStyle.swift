import SwiftUI
import ThemeUI

/// Shared shape and spacing values for ``TtcCard``, derived from the Fairway Morning "Cards & lists"
/// spec. (Parallels `TtcChipStyle` / `TtcAvatarStyle`.)
///
/// The corner shape is resolved from the theme's scale once, via ``shape(_:)``, and referenced from
/// the card's clip, its `contentShape`, and ``TtcCardButtonStyle`` — rather than repeating a literal
/// at each call site.
enum TtcCardStyle {

    /// Width of the ``TtcAccentCard`` rail — Fairway Morning's 4px accent bar.
    static let accentWidth: CGFloat = 4

    /// Default card content padding — Fairway Morning `space-md` (16dp) on all sides.
    static let contentPadding = EdgeInsets(top: 16, leading: 16, bottom: 16, trailing: 16)

    /// Zero content padding, for "sectioned" cards whose children must run edge to edge — a header
    /// strip, a full-bleed divider, or list rows that supply their own padding. Those sections cannot
    /// reach the card's edges if the card itself is inset.
    static let noContentPadding = EdgeInsets(top: 0, leading: 0, bottom: 0, trailing: 0)

    /// The card's clip/hit shape — Fairway Morning `radius-md`, the theme's `medium` (12pt).
    ///
    /// Takes the scale as a parameter rather than reading the environment, matching ``colors(_:_:)``.
    /// That is load-bearing for ``TtcCardButtonStyle``: `@EnvironmentObject` is not injected into a
    /// `ButtonStyle`'s `makeBody`, so the owning view has to resolve the shape and pass it in.
    ///
    /// The Android twin is `TtcCardDefaults.Shape`.
    static func shape(_ shapes: Shapes) -> AnyShape { shapes.medium }

    /// Container background + content/foreground colors for `role`.
    ///
    /// Like ``TtcChipStyle`` (and unlike ``TtcButtonStyle``), every tinted role uses its *container*
    /// tone, never the solid role color — that is what the design's inline banners are built from,
    /// with `.error` the "we don't recognize that email" banner and `.secondary` the gold "that
    /// email is already in use" warning.
    ///
    /// `.neutral` is the design's base card (`surfaceContainer` / `onSurface` — note `onSurface`,
    /// not `onSurfaceVariant`; muted content inside a card picks the variant itself).
    static func colors(_ role: TtcColorRole, _ scheme: ThemeColors) -> (background: Color, foreground: Color) {
        switch role {
        case .primary:   (scheme.primaryContainer, scheme.onPrimaryContainer)
        case .secondary: (scheme.secondaryContainer, scheme.onSecondaryContainer)
        case .tertiary:  (scheme.tertiaryContainer, scheme.onTertiaryContainer)
        case .error:     (scheme.errorContainer, scheme.onErrorContainer)
        case .neutral:   (scheme.surfaceContainer, scheme.onSurface)
        }
    }

    /// The rail color for a ``TtcAccentCard`` of the given `role`.
    ///
    /// Unlike ``colors(_:_:)`` these are the **solid** role tones, not the container tones — the
    /// rail is a 4pt signal against the card's neutral fill, so it needs the full-strength color.
    /// (Same treatment ``TtcAvatarStyle/toneColors(_:_:)`` gives `.primary` and `.tertiary`.) The
    /// rail carries no content, so the solid gold is safe here in a way it is not for a filled
    /// button or avatar.
    ///
    /// `.neutral` is the one role in the design system with no treatment in its component: a neutral
    /// rail on a neutral card fill reads as no rail at all, defeating the point of an accent. It
    /// falls back to `.primary`, the design's shipped treatment (the "New invite" card). Handled as
    /// an explicit case rather than a `default:` so that adding a ``TtcColorRole`` still breaks the
    /// build here and forces a decision.
    static func accentColor(_ role: TtcColorRole, _ scheme: ThemeColors) -> Color {
        switch role {
        case .primary:   scheme.primary
        case .secondary: scheme.secondary
        case .tertiary:  scheme.tertiary
        case .error:     scheme.error
        case .neutral:   scheme.primary
        }
    }
}

/// The `ButtonStyle` used when a ``TtcCard`` is tappable (`onClick != nil`). The card chrome lives on
/// the card itself; this only adds the press feedback + hit shape, matching `TtcChipButtonStyle`.
struct TtcCardButtonStyle: ButtonStyle {
    /// The card's hit shape, resolved from the theme by ``TtcCard`` and passed in — a `ButtonStyle`
    /// cannot read `@EnvironmentObject` in `makeBody`.
    let shape: AnyShape

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .contentShape(shape)
            .opacity(configuration.isPressed ? 0.85 : 1)
    }
}
