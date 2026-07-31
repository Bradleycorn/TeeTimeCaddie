package net.bradball.teetimecaddie.android.ui.common.forms

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.features.auth.AR

/**
 * A password field from the Fairway Morning design system.
 *
 * A thin wrapper over [TtcTextField] that masks the input and adds a trailing eye / eye-slash reveal
 * toggle. The reveal state is owned here; everything else (label, hint, error, styling) comes from
 * [TtcTextField]. For a full-width field, pass `Modifier.fillMaxWidth()`.
 *
 * @param value The current text.
 * @param onValueChange Invoked with the new text on each edit.
 * @param modifier Modifier for the field.
 * @param label The floating field label. Defaults to the shared "Password" string.
 * @param hint Optional supporting/helper text (hidden when [error] is set).
 * @param error Optional error message. When non-null the field renders in its error state.
 * @param enabled Whether the field is enabled.
 */
@Composable
fun TtcPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(AR.strings.field_label_password.resourceId),
    hint: String? = null,
    error: String? = null,
    enabled: Boolean = true,
) {
    var revealed by remember { mutableStateOf(false) }

    TtcTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        hint = hint,
        error = error,
        enabled = enabled,
        secure = !revealed,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = if (revealed) TtcIcons.VISIBILITY_OFF else TtcIcons.VISIBILITY,
        onTrailingClick = { revealed = !revealed },
        trailingContentDescription =
            stringResource(AR.strings.content_description_password_toggle.resourceId),
    )
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcPasswordFieldPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                TtcPasswordField(
                    value = "fairway",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                )
                TtcPasswordField(
                    value = "nope",
                    onValueChange = {},
                    error = "That password doesn't match. Try again.",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
