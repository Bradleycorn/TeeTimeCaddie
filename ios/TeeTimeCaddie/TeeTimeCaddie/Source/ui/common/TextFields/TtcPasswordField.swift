import SwiftUI
import TeeTimeCaddieKit

/// A password field from the Fairway Morning design system.
///
/// Shares the field chrome (label, card, border, support row) with ``TtcTextField`` via
/// ``TtcTextFieldContainer``, and injects the secure-entry control: a plain `TextField` and a masked
/// `SecureField` overlaid in a `ZStack`, with a trailing eye / eye-slash reveal toggle.
///
/// Keeping BOTH fields in the tree (rather than swapping one for the other) is what lets focus — and
/// therefore the keyboard — survive a reveal: toggling only moves focus between two always-present
/// fields via the enum `@FocusState`. This overlay lives here, not in ``TtcTextField``, so plain text
/// fields never instantiate a `SecureField` they don't need.
struct TtcPasswordField: View {
    private enum Field { case plain, secure }

    @Binding private var text: String
    private let label: String
    private let hint: String?
    private let error: String?

    @State private var revealed = false
    @FocusState private var focusedField: Field?

    /// An optional externally-owned focus binding.
    ///
    /// This field's own focus is an *enum* — it has two controls and tracks which one holds the
    /// keyboard — while a caller only wants to say "focus the password". So rather than shadowing
    /// the internal state the way ``TtcTextField`` does, the two are kept in step by the pair of
    /// `onChange` handlers below.
    private let focused: FocusState<Bool>.Binding?

    init(
        text: Binding<String>,
        label: String = AR.strings().field_label_password.desc().localized(),
        hint: String? = nil,
        error: String? = nil,
        focused: FocusState<Bool>.Binding? = nil
    ) {
        self._text = text
        self.label = label
        self.hint = hint
        self.error = error
        self.focused = focused
    }

    /// The field that should hold focus given the current reveal state.
    private var activeField: Field { revealed ? .plain : .secure }
    
    private var textFieldIputStyle: TtcTextFieldInputStyle {
        TtcTextFieldInputStyle(isError: error?.isEmpty == false, textContentType: .password)
    }

    var body: some View {
        TtcTextFieldContainer(
            label: label,
            hint: hint,
            error: error,
            isFocused: focusedField != nil,
            trailingIcon: revealed ? .symbol(.eyeSlash) : .symbol(.eye),
            onTrailingTap: { revealed.toggle() },
            trailingAccessibilityLabel:
                AR.strings().content_description_password_toggle.desc().localized(),
            onActivate: { focusedField = activeField }
        ) {
            ZStack {
                TextField("", text: $text)
                    .modifier(textFieldIputStyle)
                    .focused($focusedField, equals: .plain)
                    .opacity(revealed ? 1 : 0)
                    .allowsHitTesting(revealed)
                SecureField("", text: $text)
                    .modifier(textFieldIputStyle)
                    .focused($focusedField, equals: .secure)
                    .opacity(revealed ? 0 : 1)
                    .allowsHitTesting(!revealed)
            }
        }
        // Keep the keyboard up across a reveal: move focus to the now-visible field (only when
        // already focused) instead of letting the change drop first responder.
        .onChange(of: revealed) {
            if focusedField != nil { focusedField = activeField }
        }
        // Mirror the external Bool onto the internal enum, and back.
        //
        // Both directions are **equality-guarded**. Without the guard each handler would write a
        // value the other observes, and the two would ping-pong: setting the enum fires the first
        // handler, which sets the Bool, which fires the second, which sets the enum again. The
        // guards make each write a no-op once the two already agree, so the exchange settles after
        // one hop.
        .onChange(of: focusedField) {
            guard let focused else { return }
            let hasFocus = focusedField != nil
            if focused.wrappedValue != hasFocus { focused.wrappedValue = hasFocus }
        }
        .onChange(of: focused?.wrappedValue) {
            guard let focused else { return }
            let hasFocus = focusedField != nil
            if focused.wrappedValue != hasFocus {
                focusedField = focused.wrappedValue ? activeField : nil
            }
        }
    }
 }

// MARK: - Previews

#Preview("Light") {
    TeeTimeCaddieTheme {
        TtcPasswordFieldPreviews()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        TtcPasswordFieldPreviews()
    }
    .preferredColorScheme(.dark)
}

fileprivate struct TtcPasswordFieldPreviews: View {
    @State private var password = "fairway"
    @State private var badPassword = "nope"

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            TtcPasswordField(text: $password)
            TtcPasswordField(
                text: $badPassword,
                error: "That password doesn't match. Try again."
            )
        }
        .padding()
    }
}
