package net.bradball.teetimecaddie.android.ui.common.forms

import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Shared color and sizing values for [TtcTextField], derived from the Fairway Morning "Text fields"
 * spec (Material 3 *filled* field: `surfaceContainerHighest` fill, top-rounded corners, a bottom
 * indicator that tints to `primary` on focus / `error` on error, and a floating label). Colors always
 * resolve to semantic Material 3 roles — never raw hex.
 *
 * Mirrors the structure of `TtcButtonDefaults`.
 */
internal object TtcTextFieldDefaults {

    /**
     * Email entry: the email keyboard, no auto-capitalisation, no autocorrect, and Next so the
     * person can move on to the password.
     *
     * Living here rather than at each call site is deliberate — the story requires that neither
     * the email nor the password field capitalises or corrects, and a default cannot be forgotten.
     */
    /** The error glyph in the supporting-text row, and its gap from the message. */
    val SupportingIconSize = 16.dp
    val SupportingIconSpacing = 4.dp

    val EmailKeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email,
        capitalization = KeyboardCapitalization.None,
        autoCorrectEnabled = false,
        imeAction = ImeAction.Next,
    )

    /** Password entry: password keyboard, no capitalisation or autocorrect, Done to submit. */
    val PasswordKeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Password,
        capitalization = KeyboardCapitalization.None,
        autoCorrectEnabled = false,
        imeAction = ImeAction.Done,
    )

    /** A person's name: capitalise each word, no autocorrect, Next on to the phone number. */
    val NameKeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        capitalization = KeyboardCapitalization.Words,
        autoCorrectEnabled = false,
        imeAction = ImeAction.Next,
    )

    /** Phone entry: the numeric keypad, Done to submit. */
    val PhoneKeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Phone,
        imeAction = ImeAction.Done,
    )

    /** Leading icon size for text fields (Fairway Morning: 22dp). */
    val IconSize = 22.dp

    /**
     * Container shape — top corners rounded to `radius-xs` (the theme's [Shapes.extraSmall], 4dp),
     * bottom corners squared off, matching the Material 3 filled field in the design.
     *
     * The squared bottom is what makes room for the focus/error indicator, so the token is taken for
     * the top corners only. iOS has no bottom indicator and rounds all four corners at
     * `shapes.medium` instead — see `TtcTextFieldStyle.shape(_:)`. That divergence is deliberate:
     * each platform renders its own idiomatic field.
     */
    val Shape: Shape
        @Composable get() = MaterialTheme.shapes.extraSmall.copy(
            bottomStart = ZeroCornerSize,
            bottomEnd = ZeroCornerSize,
        )

    /**
     * [TextFieldColors] mapping the Fairway Morning field states onto Material 3 roles:
     * - container: `surfaceContainerHighest` in every state (the fill persists on focus/error/disabled)
     * - indicator/label/leading icon: `onSurfaceVariant` at rest → `primary` on focus → `error` on error
     * - input text: `onSurface`; placeholder & supporting text: `onSurfaceVariant`, `error` when in error
     */
    @Composable
    fun colors(): TextFieldColors {
        val scheme = MaterialTheme.colorScheme
        return TextFieldDefaults.colors(
            // Text
            focusedTextColor = scheme.onSurface,
            unfocusedTextColor = scheme.onSurface,
            disabledTextColor = scheme.onSurfaceVariant,
            errorTextColor = scheme.onSurface,
            // Container (fill persists across states)
            focusedContainerColor = scheme.surfaceContainerHighest,
            unfocusedContainerColor = scheme.surfaceContainerHighest,
            disabledContainerColor = scheme.surfaceContainerHighest,
            errorContainerColor = scheme.surfaceContainerHighest,
            // Cursor
            cursorColor = scheme.primary,
            errorCursorColor = scheme.error,
            // Bottom indicator
            focusedIndicatorColor = scheme.primary,
            unfocusedIndicatorColor = scheme.onSurfaceVariant,
            disabledIndicatorColor = scheme.outlineVariant,
            errorIndicatorColor = scheme.error,
            // Leading icon (tracks the label color)
            focusedLeadingIconColor = scheme.primary,
            unfocusedLeadingIconColor = scheme.onSurfaceVariant,
            disabledLeadingIconColor = scheme.onSurfaceVariant,
            errorLeadingIconColor = scheme.error,
            // Trailing icon (e.g. the password reveal toggle) — subtle, independent of focus
            focusedTrailingIconColor = scheme.onSurfaceVariant,
            unfocusedTrailingIconColor = scheme.onSurfaceVariant,
            disabledTrailingIconColor = scheme.onSurfaceVariant,
            errorTrailingIconColor = scheme.error,
            // Label
            focusedLabelColor = scheme.primary,
            unfocusedLabelColor = scheme.onSurfaceVariant,
            disabledLabelColor = scheme.onSurfaceVariant,
            errorLabelColor = scheme.error,
            // Placeholder
            focusedPlaceholderColor = scheme.onSurfaceVariant,
            unfocusedPlaceholderColor = scheme.onSurfaceVariant,
            disabledPlaceholderColor = scheme.onSurfaceVariant,
            // Supporting text (hint / error message)
            focusedSupportingTextColor = scheme.onSurfaceVariant,
            unfocusedSupportingTextColor = scheme.onSurfaceVariant,
            disabledSupportingTextColor = scheme.onSurfaceVariant,
            errorSupportingTextColor = scheme.error,
        )
    }
}
