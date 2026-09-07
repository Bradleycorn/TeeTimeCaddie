import SwiftUI
import ThemeUI

/// Role resolution for ``TtcButton`` and ``TtcOutlinedButton``, derived from the Fairway Morning
/// "Buttons & actions" spec. (The Android twin is `TtcButtonDefaults`.)
///
/// Buttons are the highest-emphasis component, and this is the only place a ``TtcColorRole``
/// resolves to a **solid** container tone for a fill: `.primary` is the green CTA and `.error` the
/// destructive one. The remaining roles use the softer container tones that ``TtcChipStyle`` and
/// ``TtcCardStyle`` use throughout — the design's "tonal" button.
///
/// Note `.secondary` is a container fill rather than a solid one even though it sits in the
/// high-emphasis group. The gold at full strength cannot carry legible content on top of it, so a
/// solid `secondary` is never paired with a foreground anywhere in the app.
/// ``TtcAvatarStyle/toneColors(_:_:)`` makes the same substitution for the same reason.
enum TtcButtonStyle {

    /// Button shape — the Fairway Morning pill.
    ///
    /// Deliberately **not** a `theme.shapes` token: a pill is fully round at any height, so a capsule
    /// is the value itself rather than a radius on the app's scale (see `ttcShapes` in
    /// `ui/theme/TeeTimeCaddieTheme.swift`, which has no pill slot). It therefore takes no `Shapes`
    /// parameter, unlike ``TtcCardStyle/shape(_:)`` and ``TtcChipStyle/shape(_:)``.
    ///
    /// Returned as the concrete `Capsule` rather than an `AnyShape` so that ``TtcPillButtonStyle``
    /// keeps its `strokeBorder` — only `InsettableShape` has it, and type erasure would lose that.
    ///
    /// The Android twin is `TtcButtonDefaults.Shape`, which is `CircleShape` for the same reason.
    static var shape: Capsule { Capsule() }

    /// Filled container background + content/foreground colors for `role`.
    static func filledColors(_ role: TtcColorRole, _ scheme: ThemeColors) -> (background: Color, foreground: Color) {
        switch role {
        case .primary:   (scheme.primary, scheme.onPrimary)
        case .secondary: (scheme.secondaryContainer, scheme.onSecondaryContainer)
        case .tertiary:  (scheme.tertiaryContainer, scheme.onTertiaryContainer)
        case .error:     (scheme.error, scheme.onError)
        case .neutral:   (scheme.surfaceContainerHighest, scheme.onSurfaceVariant)
        }
    }

    /// Outlined text/content color. `.neutral` (the design default) uses `onSurface`; every other
    /// role tints to that role color.
    static func outlinedForeground(_ role: TtcColorRole, _ scheme: ThemeColors) -> Color {
        switch role {
        case .primary:   scheme.primary
        case .secondary: scheme.secondary
        case .tertiary:  scheme.tertiary
        case .error:     scheme.error
        case .neutral:   scheme.onSurface
        }
    }

    /// Outlined border color. Tinted roles use their role color for the border; `.neutral` uses the
    /// subtle `outline` role, deliberately distinct from its `onSurface` text — the design's default
    /// outlined button pairs strong text with a quiet border.
    ///
    /// Written as explicit cases rather than a `default:` arm so that adding a ``TtcColorRole``
    /// breaks the build here and forces a decision.
    static func outlinedBorder(_ role: TtcColorRole, _ scheme: ThemeColors) -> Color {
        switch role {
        case .primary, .secondary, .tertiary, .error: outlinedForeground(role, scheme)
        case .neutral: scheme.outline
        }
    }
}

/// The pill `ButtonStyle` shared by ``TtcButton`` and ``TtcOutlinedButton``.
///
/// Implements the Fairway Morning pill: capsule shape, `labelLarge`-weight text, comfortable or
/// [dense] padding, an enabled/disabled background + foreground, and an optional 1pt border (used by
/// the outlined variant). Disabled colors are resolved by the caller and passed in.
struct TtcPillButtonStyle: ButtonStyle {
    @Environment(\.isEnabled) private var isEnabled: Bool

    let background: Color
    let foreground: Color
    let disabledBackground: Color
    let disabledForeground: Color
    /// Enabled border color, or `nil` for a borderless (filled) button.
    let border: Color?
    /// Disabled border color, or `nil`.
    let disabledBorder: Color?
    let dense: Bool

    private var padding: EdgeInsets {
        dense
            ? EdgeInsets(top: 8, leading: 14, bottom: 8, trailing: 14)
            : EdgeInsets(top: 10, leading: 18, bottom: 10, trailing: 18)
    }

    func makeBody(configuration: Configuration) -> some View {
        let bg = isEnabled ? background : disabledBackground
        let fg = isEnabled ? foreground : disabledForeground
        let borderColor = isEnabled ? border : disabledBorder

        configuration.label
            .font(.body.weight(.semibold))
            .padding(padding)
            .frame(minHeight: 20)
            .background(bg)
            .foregroundStyle(fg)
            .clipShape(TtcButtonStyle.shape)
            .overlay {
                if let borderColor {
                    TtcButtonStyle.shape.strokeBorder(borderColor, lineWidth: 1)
                }
            }
            .contentShape(TtcButtonStyle.shape)
            .opacity(configuration.isPressed ? 0.85 : 1)
    }
}

/// A leading-icon + title label used inside the Fairway Morning pill buttons.
///
/// The icon (a namespaced asset symbol, e.g. `.Icons.calendarAdd`) renders at 18pt and inherits the
/// button's foreground color.
struct TtcButtonLabel: View {
    let title: String
    let icon: ImageResource?

    var body: some View {
        HStack(spacing: 8) {
            if let icon {
                Image(icon)
                    .font(.system(size: 18))
            }
            Text(title)
        }
    }
}
