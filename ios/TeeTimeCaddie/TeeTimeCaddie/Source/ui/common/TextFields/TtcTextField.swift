import SwiftUI
import ThemeUI

/// A single-line text field from the Fairway Morning design system.
///
/// iOS renders the custom "filled-row" card (matching the Create Game screen): a `surfaceContainer`
/// rounded card with a static [label] above the input, an optional [leadingIcon], and a 2pt border
/// that tints to `primary` on focus (or `error` when [error] is set). [placeholder], [hint], and
/// [leadingIcon] are all optional. When [error] is non-null the field enters its error state and
/// shows the message (in place of [hint]) beneath the card.
///
/// The card chrome (label, border, support row) is shared with ``TtcPasswordField`` via
/// ``TtcTextFieldContainer``; this field injects a single plain `TextField` as its control. Disabled
/// state follows the standard SwiftUI idiom — apply `.disabled(true)` at the call site.
struct TtcTextField: View {
    @FocusState private var isFocused: Bool

    private let label: String
    @Binding private var text: String
    private let leadingIcon: ImageResource?
    private let placeholder: String?
    private let hint: String?
    private let error: String?
    private let keyboardType: UIKeyboardType
    private let textContentType: UITextContentType?
    private let autocapitalization: TextInputAutocapitalization
    private let autocorrectionDisabled: Bool

    init(
        _ label: String,
        text: Binding<String>,
        leadingIcon: ImageResource? = nil,
        placeholder: String? = nil,
        hint: String? = nil,
        error: String? = nil,
        keyboardType: UIKeyboardType = .default,
        textContentType: UITextContentType? = nil,
        autocapitalization: TextInputAutocapitalization = .never,
        autocorrectionDisabled: Bool = true
    ) {
        self.label = label
        self._text = text
        self.leadingIcon = leadingIcon
        self.placeholder = placeholder
        self.hint = hint
        self.error = error
        self.keyboardType = keyboardType
        self.textContentType = textContentType
        self.autocapitalization = autocapitalization
        self.autocorrectionDisabled = autocorrectionDisabled
    }

    var body: some View {
        TtcTextFieldContainer(
            label: label,
            leadingIcon: leadingIcon,
            hint: hint,
            error: error,
            isFocused: isFocused,
            onActivate: { isFocused = true }
        ) {
            TextField(placeholder ?? "", text: $text)
                .modifier(
                    TtcTextFieldInputStyle(
                        isError: error?.isEmpty == false,
                        keyboardType: keyboardType,
                        textContentType: textContentType,
                        autocapitalization: autocapitalization,
                        autocorrectionDisabled: autocorrectionDisabled
                    )
                )
                .focused($isFocused)
        }
    }
}

// MARK: - Previews

#Preview("Light") {
    TeeTimeCaddieTheme {
        TtcTextFieldPreviews()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        TtcTextFieldPreviews()
    }
    .preferredColorScheme(.dark)
}

fileprivate struct TtcTextFieldPreviews: View {
    @State private var email = ""
    @State private var name = "Dana Park"
    @State private var course = "e.g. Pebble Beach"
    @State private var phone = "(555) 010-1010"

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            TtcTextField(
                "Email",
                text: $email,
                leadingIcon: .Icons.calendar,
                placeholder: "you@golf.app",
                keyboardType: .emailAddress
            )
            TtcTextField("Full name", text: $name, autocapitalization: .words)
            TtcTextField(
                "Course",
                text: $course,
                hint: "We match case-insensitively against existing courses."
            )
            TtcTextField(
                "Mobile number",
                text: $phone,
                error: "Already linked to another account",
                keyboardType: .phonePad
            )
            TtcTextField("Disabled", text: .constant("Disabled"))
                .disabled(true)
        }
        .padding()
    }
}
