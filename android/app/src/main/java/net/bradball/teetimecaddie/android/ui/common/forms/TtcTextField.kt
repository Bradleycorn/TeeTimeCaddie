package net.bradball.teetimecaddie.android.ui.common.forms

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons

/**
 * A single-line text field from the Fairway Morning design system.
 *
 * Renders the Material 3 *filled* field: a `surfaceContainerHighest` fill with top-rounded corners
 * and a bottom indicator that tints to `primary` on focus (or `error` when [error] is set), with a
 * floating [label]. [leadingIcon], [placeholder], and [hint] are all optional. When [error] is
 * non-null the field enters its error state and shows the message in place of [hint].
 *
 * For a full-width field, pass `Modifier.fillMaxWidth()`.
 *
 * @param value The current text.
 * @param onValueChange Invoked with the new text on each edit.
 * @param label The floating field label (always shown).
 * @param modifier Modifier for the field.
 * @param leadingIcon Optional leading [TtcIcons] icon, tinted with the label color.
 * @param placeholder Optional placeholder shown once the label floats.
 * @param hint Optional supporting/helper text shown beneath the field (hidden when [error] is set).
 * @param error Optional error message. When non-null the field renders in its error state.
 * @param enabled Whether the field is enabled.
 * @param secure When true, the input is masked (e.g. for a password). See [TtcPasswordField], which
 *   wraps this field to add a reveal toggle.
 * @param trailingIcon Optional trailing [TtcIcons] icon rendered as a tappable [IconButton].
 * @param onTrailingClick Invoked when the trailing icon is tapped (ignored when [trailingIcon] is null).
 * @param trailingContentDescription Accessibility description for the trailing icon.
 * @param keyboardOptions Software-keyboard configuration (type, capitalization, IME action, …).
 */
@Composable
fun TtcTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: TtcIcons? = null,
    placeholder: String? = null,
    hint: String? = null,
    error: String? = null,
    enabled: Boolean = true,
    secure: Boolean = false,
    trailingIcon: TtcIcons? = null,
    onTrailingClick: (() -> Unit)? = null,
    trailingContentDescription: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    painter = it.painter,
                    contentDescription = null,
                    modifier = Modifier.size(TtcTextFieldDefaults.IconSize),
                )
            }
        },
        trailingIcon = trailingIcon?.let {
            {
                IconButton(onClick = { onTrailingClick?.invoke() }) {
                    Icon(
                        painter = it.painter,
                        contentDescription = trailingContentDescription,
                        modifier = Modifier.size(TtcTextFieldDefaults.IconSize),
                    )
                }
            }
        },
        supportingText = supportingText(error = error, hint = hint),
        isError = error != null,
        visualTransformation = if (secure) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        shape = TtcTextFieldDefaults.Shape,
        colors = TtcTextFieldDefaults.colors(),
        keyboardOptions = keyboardOptions,
    )
}

/**
 * Builds the supporting-text slot: the [error] message (when set) otherwise the [hint], or null when
 * neither is present. The error message inherits the error color from [TtcTextFieldDefaults.colors].
 *
 * Note: the Fairway Morning error row includes a small leading error glyph. `TtcIcons` has no error
 * icon yet (and the full Material Icons library is intentionally not imported), so the glyph is
 * omitted for now — see the plan's follow-ups.
 */
private fun supportingText(error: String?, hint: String?): (@Composable () -> Unit)? {
    val text = error ?: hint ?: return null
    return { Text(text) }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcTextFieldPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                TtcTextField(
                    value = "",
                    onValueChange = {},
                    label = "Email",
                    leadingIcon = TtcIcons.CALENDAR,
                    placeholder = "you@golf.app",
                    modifier = Modifier.fillMaxWidth(),
                )
                TtcTextField(
                    value = "Dana Park",
                    onValueChange = {},
                    label = "Full name",
                    modifier = Modifier.fillMaxWidth(),
                )
                TtcTextField(
                    value = "e.g. Pebble Beach",
                    onValueChange = {},
                    label = "Course",
                    hint = "We match case-insensitively against existing courses.",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(name = "States - Light", showBackground = true)
@Preview(name = "States - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcTextFieldStatesPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                TtcTextField(
                    value = "(555) 010-1010",
                    onValueChange = {},
                    label = "Mobile number",
                    error = "Already linked to another account",
                    modifier = Modifier.fillMaxWidth(),
                )
                TtcTextField(
                    value = "Disabled",
                    onValueChange = {},
                    label = "Course",
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
