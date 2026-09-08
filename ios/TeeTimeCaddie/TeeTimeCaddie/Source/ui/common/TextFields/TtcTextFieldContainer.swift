import SwiftUI
import ThemeUI

/// The shared "chrome" for the Fairway Morning text fields: the `radius-md`-rounded `surfaceContainer` card, the
/// static label, an optional leading icon, an optional trailing button, the focus/error border, and
/// the supporting text row. The actual input control is injected via [input] so that each field only
/// pays for what it needs — ``TtcTextField`` injects a single `TextField`, while ``TtcPasswordField``
/// injects a masked `ZStack` overlay. Focus lives with the injected control; the container is told
/// [isFocused] for its coloring and calls [onActivate] when the surrounding card is tapped.
struct TtcTextFieldContainer<Input: View>: View {
    @EnvironmentObject private var theme: AppTheme
    @Environment(\.isEnabled) private var isEnabled: Bool

    let label: String
    var leadingIcon: ImageResource? = nil
    var hint: String? = nil
    var error: String? = nil
    /// Whether the injected input currently holds focus (owned by the caller's `@FocusState`).
    let isFocused: Bool
    var trailingIcon: ImageResource? = nil
    var onTrailingTap: (() -> Void)? = nil
    var trailingAccessibilityLabel: String? = nil
    /// Called when the card chrome (padding/label) is tapped, so the caller can focus its control.
    let onActivate: () -> Void
    @ViewBuilder let input: () -> Input

    var body: some View {
        let scheme = theme.colorScheme
        let shape = TtcTextFieldStyle.shape(theme.shapes)
        let hasError = error != nil
        let labelColor = hasError ? scheme.error : (isFocused ? scheme.primary : scheme.onSurfaceVariant)
        let borderColor: Color = hasError ? scheme.error : (isFocused ? scheme.primary : .clear)

        VStack(alignment: .leading, spacing: 6) {
            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.caption.weight(.medium))
                    .foregroundStyle(labelColor)

                HStack(spacing: 10) {
                    if let leadingIcon {
                        Image(leadingIcon)
                            .font(.system(size: 22))
                            .foregroundStyle(scheme.onSurfaceVariant)
                    }
                    input()
                    if let trailingIcon {
                        Button {
                            onTrailingTap?()
                        } label: {
                            Image(trailingIcon)
                                .font(.system(size: 22))
                                .foregroundStyle(scheme.onSurfaceVariant)
                        }
                        .buttonStyle(.plain)
                        .frame(width: 36, height: 36)
                        .accessibilityLabel(trailingAccessibilityLabel ?? "")
                    }
                }
            }
            .padding(EdgeInsets(top: 8, leading: 16, bottom: 10, trailing: 16))
            .background(scheme.surfaceContainer)
            .overlay {
                // `strokeBorder` needs an `InsettableShape`, and the theme's shapes are type-erased
                // to `AnyShape`. `stroke` centers the line on the path instead, so draw it at double
                // width and let the trailing `clipShape` remove the outer half — the same 2pt inset
                // border `strokeBorder(_, lineWidth: 2)` produced.
                shape.stroke(borderColor, lineWidth: 4)
            }
            .clipShape(shape)
            .contentShape(shape)
            .onTapGesture { onActivate() }

            TtcTextFieldSupport(error: error, hint: hint)
        }
        .opacity(isEnabled ? 1 : 0.5)
    }
}

/// Shared styling for a Fairway Morning field's input control (`TextField` / `SecureField`): body
/// font, `onSurface` text, cursor tint (`error` when in an error state, else `primary`), and keyboard
/// configuration. Applied by each field so the injected control arrives fully styled.
struct TtcTextFieldInputStyle: ViewModifier {
    @EnvironmentObject private var theme: AppTheme

    let isError: Bool
    let keyboardType: UIKeyboardType
    let textContentType: UITextContentType?
    let autocapitalization: TextInputAutocapitalization
    let autocorrectionDisabled: Bool

    init(isError: Bool = false, keyboardType: UIKeyboardType = .default, textContentType: UITextContentType? = nil, autocapitalization: TextInputAutocapitalization = .never, autocorrectionDisabled: Bool = true) {
        self.isError = isError
        self.keyboardType = keyboardType
        self.textContentType = textContentType
        self.autocapitalization = autocapitalization
        self.autocorrectionDisabled = autocorrectionDisabled
    }
        
    func body(content: Content) -> some View {
        let scheme = theme.colorScheme
        let accent = isError ? scheme.error : scheme.primary
        content
            .font(.body)
            .foregroundStyle(scheme.onSurface)
            .tint(accent)
            .keyboardType(keyboardType)
            .textContentType(textContentType)
            .textInputAutocapitalization(autocapitalization)
            .autocorrectionDisabled(autocorrectionDisabled)
    }
}

/// The supporting-text row beneath a field: the [error] message when set (with a leading error glyph,
/// in the `error` color), otherwise the [hint] (in `onSurfaceVariant`). Renders nothing when both are
/// absent. Uses the SF Symbol `exclamationmark.circle.fill`; a custom error asset is a later follow-up.
fileprivate struct TtcTextFieldSupport: View {
    @EnvironmentObject private var theme: AppTheme
    let error: String?
    let hint: String?

    var body: some View {
        let scheme = theme.colorScheme
        if let error, !error.isEmpty {
            HStack(spacing: 6) {
                Image(systemName: "exclamationmark.circle.fill")
                    .font(.system(size: 12))
                Text(error)
                    .font(.caption)
            }
            .foregroundStyle(scheme.error)
            .padding(.horizontal, 16)
        } else if let hint {
            Text(hint)
                .font(.caption)
                .foregroundStyle(scheme.onSurfaceVariant)
                .padding(.horizontal, 16)
        }
    }
}

#Preview("Container") {
    TeeTimeCaddieTheme {
        TtcTextFieldContainer(
            label: "Email",
            leadingIcon: .Icons.calendar,
            hint: "A sample of the shared field chrome.",
            isFocused: false,
            onActivate: {}
        ) {
            Text("input goes here")
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding()
    }
}
