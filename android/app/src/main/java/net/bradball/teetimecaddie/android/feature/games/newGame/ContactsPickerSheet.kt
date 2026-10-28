package net.bradball.teetimecaddie.android.feature.games.newGame

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.theme.toColorRole
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcAvatar
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.sheets.TtcBottomSheet
import net.bradball.teetimecaddie.core.models.InvitableContact
import net.bradball.teetimecaddie.core.models.previewInvitableContactList
import net.bradball.teetimecaddie.features.games.GMR

/**
 * The contacts picker: a search field over every contact who can be invited, one checkbox row per
 * number, and "Done (N)" in the header.
 *
 * Done applies the checks and closes the sheet. Dragging it down or tapping outside closes it
 * without applying them; there is no Cancel.
 *
 * @param onDone Called as Done closes the sheet, to apply the checks.
 * @param onDismiss Called once the sheet has closed, by any route.
 */
@Composable
fun ContactsPickerSheet(
    picker: ContactsSheet.Picker,
    onQueryChange: (String) -> Unit,
    onToggle: (InvitableContact) -> Unit,
    onDone: () -> Unit,
    onDismiss: () -> Unit,
) {
    TtcBottomSheet(
        title = stringResource(GMR.strings.contacts_picker_title.resourceId),
        onDismissRequest = onDismiss,
        contentPadding = PaddingValues(0.dp),
        action = { dismiss ->
            TextButton(
                onClick = {
                    onDone()
                    dismiss()
                },
            ) {
                Text(
                    text = stringResource(GMR.strings.contacts_picker_done.resourceId, picker.checkedIds.size.toString()),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
    ) {
        ContactsPickerBody(picker = picker, onQueryChange = onQueryChange, onToggle = onToggle)
    }
}

@Composable
private fun ContactsPickerBody(
    picker: ContactsSheet.Picker,
    onQueryChange: (String) -> Unit,
    onToggle: (InvitableContact) -> Unit,
) {
    // The search field's keyboard would otherwise outlive the sheet, covering the form.
    val keyboard = LocalSoftwareKeyboardController.current
    DisposableEffect(keyboard) {
        onDispose { keyboard?.hide() }
    }

    // Full height, so the sheet doesn't resize as a search narrows the list, and lifted above the
    // keyboard so the list stays scrollable while searching.
    Column(modifier = Modifier.fillMaxHeight().imePadding()) {
        SearchField(
            query = picker.query,
            onQueryChange = onQueryChange,
            modifier = Modifier.padding(start = 12.dp, top = 4.dp, end = 12.dp, bottom = 12.dp),
        )

        val emptyMessage = picker.emptyMessage
        if (emptyMessage != null) {
            Text(
                text = stringResource(emptyMessage.resourceId, picker.query.trim()),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                items(picker.shown, key = { it.id }) { contact ->
                    ContactRow(
                        contact = contact,
                        checked = picker.isChecked(contact),
                        onToggle = { onToggle(contact) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Search),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { field ->
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.extraLarge)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painter = TtcIcons.SEARCH.painter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(GMR.strings.contacts_picker_search.resourceId),
                            style = textStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    field()
                }
            }
        },
    )
}

/** One contact at one number. Tapping anywhere on the row toggles its checkbox. */
@Composable
private fun ContactRow(
    contact: InvitableContact,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    val number = contact.phoneLabel?.let { label ->
        stringResource(GMR.strings.contacts_picker_labelled_number.resourceId, label, contact.formattedPhone)
    } ?: contact.formattedPhone

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TtcAvatar(initials = contact.initial, color = contact.avatarTone.toColorRole())
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = number,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // The row is the toggle; the checkbox only shows its state.
        Checkbox(checked = checked, onCheckedChange = null)
    }
}

// `ModalBottomSheet` doesn't render in Studio previews (see `TtcBottomSheet`), so these preview the
// body alone.

@Preview(name = "Light", showBackground = true, heightDp = 480)
@Preview(name = "Dark", showBackground = true, heightDp = 480, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ContactsPickerPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            ContactsPickerBody(
                picker = ContactsSheet.Picker(
                    contacts = previewInvitableContactList,
                    checkedIds = listOf(previewInvitableContactList[1].id),
                ),
                onQueryChange = {},
                onToggle = {},
            )
        }
    }
}

@Preview(name = "No match", showBackground = true, heightDp = 240)
@Composable
private fun ContactsPickerNoMatchPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            ContactsPickerBody(
                picker = ContactsSheet.Picker(contacts = previewInvitableContactList, query = "zz", shown = emptyList()),
                onQueryChange = {},
                onToggle = {},
            )
        }
    }
}
