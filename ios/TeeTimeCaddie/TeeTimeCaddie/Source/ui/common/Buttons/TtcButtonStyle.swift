import SwiftUI
import ThemeUI

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
            .clipShape(Capsule())
            .overlay {
                if let borderColor {
                    Capsule().strokeBorder(borderColor, lineWidth: 1)
                }
            }
            .contentShape(Capsule())
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
