import SwiftUI

/// US phone-number input: digits in state, grouping on screen.
///
/// The Swift twin of `PhoneExtensions.kt` and `PhoneVisualTransformation.kt`. The rules are shared
/// with Android deliberately — a number typed on one platform has to read the same on the other,
/// and has to reach storage in the same shape.
///
/// **Formatting never reaches state.** `digits` is what the app holds and submits; ``formatted(_:)``
/// is only how it is drawn. That split is also what keeps the caret sane: Android hit a bug where
/// rewriting the field's value on every keystroke moved the text under the caret without moving the
/// caret, so "5025551234" typed in order came out "(502) 123-4555". ``binding(to:)`` avoids it by
/// formatting on the way out and reducing to digits on the way back in.
enum PhoneNumber {

    /// A complete US number, without a country code.
    static let length = 10

    /// Strips everything but digits, drops a leading US country code, and caps the result.
    ///
    /// ```
    /// PhoneNumber.digits("(502) 555-1234")  == "5025551234"
    /// PhoneNumber.digits("+1 502-555-1234") == "5025551234"
    /// ```
    static func digits(_ input: String) -> String {
        var digits = input.filter(\.isNumber)

        // A US area code cannot begin with 1, so a leading 1 on an 11-digit string is the country
        // code rather than part of the number.
        if digits.count == length + 1 && digits.hasPrefix("1") {
            digits.removeFirst()
        }

        return String(digits.prefix(length))
    }

    /// Groups [input]'s digits for display, **without a trailing separator**.
    ///
    /// No trailing separator is the rule that makes this usable while typing: appending `") "` the
    /// moment a third digit arrives would put punctuation ahead of the caret and make the next
    /// keystroke feel like it landed in the wrong place. So `formatted("555")` is `"(555"`, and the
    /// `") "` only appears once there is a fourth digit to sit after it.
    ///
    /// ```
    /// PhoneNumber.formatted("5")          == "(5"
    /// PhoneNumber.formatted("555")        == "(555"
    /// PhoneNumber.formatted("5551")       == "(555) 1"
    /// PhoneNumber.formatted("5551234")    == "(555) 123-4"
    /// PhoneNumber.formatted("5025551234") == "(502) 555-1234"
    /// ```
    static func formatted(_ input: String) -> String {
        let digits = Self.digits(input)
        guard !digits.isEmpty else { return "" }

        let area = String(digits.prefix(3))
        let exchange = String(digits.dropFirst(3).prefix(3))
        let line = String(digits.dropFirst(6))

        var result = "(\(area)"
        if !exchange.isEmpty { result += ") \(exchange)" }
        if !line.isEmpty { result += "-\(line)" }
        return result
    }

    /// There is deliberately **no `Binding` that formats on `get`**.
    ///
    /// It is the obvious design and it does not work: while a `TextField` is being typed into,
    /// SwiftUI keeps its own buffer and ignores a binding whose `get` returns something other than
    /// what was typed. Verified on iOS 26.5 — the state and the formatting were both correct, and
    /// the field still showed raw digits.
    ///
    /// Format in the field's own `@State` instead, from `onChange`; `CreateAccountContent` shows
    /// the shape. Android solves the same problem with a `VisualTransformation`, which separates
    /// display from value so the two never compete.
}
