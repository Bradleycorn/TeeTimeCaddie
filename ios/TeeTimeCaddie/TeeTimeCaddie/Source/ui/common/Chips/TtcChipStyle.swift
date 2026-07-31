import SwiftUI

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
/// (transparent with a 1pt border). Orthogonal to ``TtcChipColor`` and ``TtcChipSize``.
enum TtcChipVariant {
    case filled
    case outlined
}

/// The shared chip "chrome" for the Fairway Morning chips: a leading-icon + label row on an
/// 8dp-rounded fill, with an optional 1pt border (used by the `.outlined` ``TtcChip`` variant).
///
/// The 14pt icon and `caption`-weight label inherit the chip's [foreground] color. Colors + border are
/// resolved by the caller from the theme and passed in (like `TtcButtonLabel`), so the same chrome
/// serves both the filled and outlined chips, interactive or static.
struct TtcChipLabel: View {
    let text: String
    let icon: ImageResource?
    let size: TtcChipSize
    let background: Color
    let foreground: Color
    /// Border color, or `nil` for a borderless (filled) chip.
    let border: Color?

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
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay {
            if let border {
                RoundedRectangle(cornerRadius: 8).strokeBorder(border, lineWidth: 1)
            }
        }
    }
}

/// The `ButtonStyle` used when a chip is tappable (`onClick != nil`). The chip chrome lives in
/// ``TtcChipLabel``; this only adds the press feedback + hit shape.
struct TtcChipButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .contentShape(RoundedRectangle(cornerRadius: 8))
            .opacity(configuration.isPressed ? 0.85 : 1)
    }
}
