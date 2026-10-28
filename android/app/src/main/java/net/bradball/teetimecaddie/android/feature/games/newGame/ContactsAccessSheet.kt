package net.bradball.teetimecaddie.android.feature.games.newGame

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcButton
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcOutlinedButton
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.sheets.TtcBottomSheet
import net.bradball.teetimecaddie.features.games.GMR

/**
 * The explanation sheet shown before the system asks for contacts access, or, once access has
 * been turned down, the sheet that points to Settings. Both are a contacts medallion, a message,
 * and "Not now" beside the [primaryLabel] button.
 *
 * Both buttons close the sheet. [onPrimary] runs as it starts to close, so the system prompt or
 * Settings appears while the sheet slides away.
 *
 * @param paragraphs The message, one string per paragraph.
 * @param onDismiss Called once the sheet has closed, by any route.
 */
@Composable
fun ContactsAccessSheet(
    title: String,
    paragraphs: List<String>,
    primaryLabel: String,
    onPrimary: () -> Unit,
    onDismiss: () -> Unit,
) {
    TtcBottomSheet(title = title, onDismissRequest = onDismiss) { dismiss ->
        ContactsAccessBody(
            paragraphs = paragraphs,
            primaryLabel = primaryLabel,
            onNotNow = dismiss,
            onPrimary = {
                dismiss()
                onPrimary()
            },
        )
    }
}

/** The explanation sheet: why the app wants contacts, before the system asks. */
@Composable
fun ContactsExplanationSheet(onContinue: () -> Unit, onDismiss: () -> Unit) {
    ContactsAccessSheet(
        title = stringResource(GMR.strings.contacts_explanation_title.resourceId),
        paragraphs = listOf(
            stringResource(GMR.strings.contacts_explanation_reason.resourceId),
            stringResource(GMR.strings.contacts_explanation_privacy.resourceId),
        ),
        primaryLabel = stringResource(GMR.strings.contacts_continue.resourceId),
        onPrimary = onContinue,
        onDismiss = onDismiss,
    )
}

/** The access-off sheet: contacts access was turned down, and Settings can turn it back on. */
@Composable
fun ContactsAccessOffSheet(onOpenSettings: () -> Unit, onDismiss: () -> Unit) {
    ContactsAccessSheet(
        title = stringResource(GMR.strings.contacts_access_off_title.resourceId),
        paragraphs = listOf(stringResource(GMR.strings.contacts_access_off_message.resourceId)),
        primaryLabel = stringResource(GMR.strings.contacts_open_settings.resourceId),
        onPrimary = onOpenSettings,
        onDismiss = onDismiss,
    )
}

@Composable
private fun ContactsAccessBody(
    paragraphs: List<String>,
    primaryLabel: String,
    onNotNow: () -> Unit,
    onPrimary: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ContactsAccessSheetDefaults.BodyInset),
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 16.dp)
                .size(ContactsAccessSheetDefaults.MedallionSize)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = TtcIcons.CONTACTS.painter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(ContactsAccessSheetDefaults.MedallionIconSize),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            paragraphs.forEach { paragraph ->
                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(
            modifier = Modifier.padding(top = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TtcOutlinedButton(
                text = stringResource(GMR.strings.contacts_not_now.resourceId),
                onClick = onNotNow,
                modifier = Modifier.weight(1f),
            )
            TtcButton(
                text = primaryLabel,
                onClick = onPrimary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private object ContactsAccessSheetDefaults {
    /** The body sits 16dp in from the sheet's edge; the sheet's own content padding supplies 8. */
    val BodyInset = 8.dp
    val MedallionSize = 72.dp
    val MedallionIconSize = 36.dp
}

// `ModalBottomSheet` doesn't render in Studio previews (see `TtcBottomSheet`), so these preview the
// body alone.

@Preview(name = "Explanation - Light", showBackground = true)
@Preview(name = "Explanation - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ContactsExplanationPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            ContactsAccessBody(
                paragraphs = listOf(
                    stringResource(GMR.strings.contacts_explanation_reason.resourceId),
                    stringResource(GMR.strings.contacts_explanation_privacy.resourceId),
                ),
                primaryLabel = stringResource(GMR.strings.contacts_continue.resourceId),
                onNotNow = {},
                onPrimary = {},
            )
        }
    }
}

@Preview(name = "Access off", showBackground = true)
@Composable
private fun ContactsAccessOffPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            ContactsAccessBody(
                paragraphs = listOf(stringResource(GMR.strings.contacts_access_off_message.resourceId)),
                primaryLabel = stringResource(GMR.strings.contacts_open_settings.resourceId),
                onNotNow = {},
                onPrimary = {},
            )
        }
    }
}
