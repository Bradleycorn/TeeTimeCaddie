package net.bradball.teetimecaddie.android.feature.games.newGame

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.theme.TtcColorRole
import net.bradball.teetimecaddie.android.theme.toColorRole
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcAvatar
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcButton
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcOutlinedButton
import net.bradball.teetimecaddie.android.ui.common.cards.TtcCard
import net.bradball.teetimecaddie.android.ui.common.chips.TtcChip
import net.bradball.teetimecaddie.android.ui.common.chips.TtcChipColorToken
import net.bradball.teetimecaddie.android.ui.common.chips.TtcChipSize
import net.bradball.teetimecaddie.android.ui.common.forms.TtcTextField
import net.bradball.teetimecaddie.android.ui.common.forms.TtcTextFieldDefaults
import net.bradball.teetimecaddie.android.ui.common.forms.filters.PhoneVisualTransformation
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.sheets.TtcBottomSheet
import net.bradball.teetimecaddie.android.ui.common.text.stringResource
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.state.addPlayersUiState.AddPlayersUiState
import net.bradball.teetimecaddie.features.games.state.addPlayersUiState.previewAddPlayersUiState

/**
 * The Add players sheet: choose people from contacts or by name and number, then add them all.
 *
 * It has no ✕. Dragging it down or tapping outside discards the selection, through [onDismiss];
 * only *Add N players* changes the game, through [onAddChosen], once the sheet has slid away.
 */
@Composable
fun AddPlayersSheet(
    uiState: AddPlayersUiState,
    onChooseFromContacts: () -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddEntered: () -> Unit,
    onUnchoose: (NewInvitee) -> Unit,
    onAddChosen: () -> Unit,
    onDismiss: () -> Unit,
) {
    // Set when Add N players is tapped, so closing the sheet adds rather than discards.
    var adding by remember { mutableStateOf(false) }

    TtcBottomSheet(
        title = stringResource(GMR.strings.add_players_title.resourceId),
        onDismissRequest = { if (adding) onAddChosen() else onDismiss() },
        contentPadding = PaddingValues(0.dp),
        // No ✕ (design criterion 11). The spacer keeps the header the height it has with one.
        action = { Spacer(Modifier.height(AddPlayersSheetDefaults.HeaderActionHeight)) },
    ) { dismiss ->
        AddPlayersSheetBody(
            uiState = uiState,
            onChooseFromContacts = onChooseFromContacts,
            onNameChange = onNameChange,
            onPhoneChange = onPhoneChange,
            onAddEntered = onAddEntered,
            onUnchoose = onUnchoose,
            onAddChosen = {
                adding = true
                dismiss()
            },
            modifier = Modifier.fillMaxHeight(AddPlayersSheetDefaults.HeightFraction),
        )
    }
}

@Composable
private fun AddPlayersSheetBody(
    uiState: AddPlayersUiState,
    onChooseFromContacts: () -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddEntered: () -> Unit,
    onUnchoose: (NewInvitee) -> Unit,
    onAddChosen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.imePadding()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(AddPlayersSheetDefaults.BodyPadding),
        ) {
            ContactsRow(summary = uiState.contactsSummary?.let { stringResource(it) }, onClick = onChooseFromContacts)
            Text(
                text = stringResource(GMR.strings.add_players_enter_prompt.resourceId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    top = AddPlayersSheetDefaults.PromptTopPadding,
                    bottom = AddPlayersSheetDefaults.FieldSpacing,
                ),
            )
            EntryForm(
                uiState = uiState,
                onNameChange = onNameChange,
                onPhoneChange = onPhoneChange,
                onAdd = onAddEntered,
            )
        }
        Tray(uiState = uiState, onUnchoose = onUnchoose, onAddChosen = onAddChosen)
    }
}

/** *Choose from contacts*, with how many have been picked once any are. */
@Composable
private fun ContactsRow(summary: String?, onClick: () -> Unit) {
    TtcCard(onClick = onClick, contentPadding = AddPlayersSheetDefaults.ContactsRowPadding) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AddPlayersSheetDefaults.RowSpacing),
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                Icon(
                    painter = TtcIcons.CONTACTS.painter,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(AddPlayersSheetDefaults.ContactsIconPadding)
                        .size(AddPlayersSheetDefaults.ContactsIconSize),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(GMR.strings.add_players_choose_from_contacts.resourceId),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                if (summary != null) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                painter = TtcIcons.CHEVRON_RIGHT.painter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Name and Phone number, then the "selected" message and Add. */
@Composable
private fun EntryForm(
    uiState: AddPlayersUiState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    val nameFocus = remember { FocusRequester() }
    val phoneFocus = remember { FocusRequester() }

    // After each successful Add the form clears; put the cursor back in Name for the next person.
    LaunchedEffect(uiState.chosenMessage) {
        if (uiState.chosenMessage != null) nameFocus.requestFocus()
    }

    Column(verticalArrangement = Arrangement.spacedBy(AddPlayersSheetDefaults.FieldSpacing)) {
        TtcTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = stringResource(GMR.strings.add_players_field_name.resourceId),
            leadingIcon = TtcIcons.PERSON_OUTLINE,
            placeholder = stringResource(GMR.strings.add_players_field_name_placeholder.resourceId),
            error = uiState.nameError?.let { stringResource(it.resourceId) },
            keyboardOptions = TtcTextFieldDefaults.NameKeyboardOptions.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { phoneFocus.requestFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(nameFocus),
        )
        TtcTextField(
            // Raw digits in; the grouping is drawn, as on the create-account phone field.
            value = uiState.phone,
            onValueChange = onPhoneChange,
            visualTransformation = PhoneVisualTransformation,
            label = stringResource(GMR.strings.add_players_field_phone.resourceId),
            leadingIcon = TtcIcons.PHONE,
            placeholder = stringResource(GMR.strings.add_players_field_phone_placeholder.resourceId),
            error = uiState.phoneError?.let { stringResource(it.resourceId) },
            keyboardOptions = TtcTextFieldDefaults.PhoneKeyboardOptions.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAdd() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(phoneFocus),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AddPlayersSheetDefaults.RowSpacing),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                uiState.chosenMessage?.let { message ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AddPlayersSheetDefaults.MessageIconSpacing),
                    ) {
                        Icon(
                            painter = TtcIcons.CHECK_CIRCLE.painter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(AddPlayersSheetDefaults.MessageIconSize),
                        )
                        Text(
                            text = stringResource(message),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            TtcOutlinedButton(
                text = stringResource(GMR.strings.add_players_add_button.resourceId),
                onClick = onAdd,
                color = TtcColorRole.Primary,
                icon = TtcIcons.ADD,
                dense = true,
            )
        }
    }
}

/** The selection's chips, when there are any, above *Add N players*. */
@Composable
private fun Tray(
    uiState: AddPlayersUiState,
    onUnchoose: (NewInvitee) -> Unit,
    onAddChosen: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large.copy(bottomStart = ZeroCornerSize, bottomEnd = ZeroCornerSize),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AddPlayersSheetDefaults.TrayPadding),
            verticalArrangement = Arrangement.spacedBy(AddPlayersSheetDefaults.TraySpacing),
        ) {
            if (uiState.chosen.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AddPlayersSheetDefaults.ChipSpacing),
                ) {
                    uiState.chosen.forEach { person ->
                        ChosenPlayerChip(
                            name = person.firstName,
                            initials = person.initial,
                            avatarColor = person.avatarTone.toColorRole(),
                            removeLabel = stringResource(GMR.strings.add_players_remove_chip.resourceId, person.name),
                            onClick = { onUnchoose(person) },
                        )
                    }
                }
            }
            TtcButton(
                text = stringResource(uiState.addPlayersLabel),
                onClick = onAddChosen,
                enabled = uiState.canAddPlayers,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * A chosen person in the tray: their avatar, first name and a ✕. Tapping anywhere on it unchooses
 * them, and a screen reader hears it as one button, [removeLabel].
 */
@Composable
private fun ChosenPlayerChip(
    name: String,
    initials: String,
    avatarColor: TtcColorRole,
    removeLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TtcChip(
        // Clearing the chip's semantics also clears the click action TtcChip adds, so restore it.
        modifier = modifier.clearAndSetSemantics {
            contentDescription = removeLabel
            role = Role.Button
            onClick {
                onClick()
                true
            }
        },
        color = TtcChipColorToken.Highest,
        size = TtcChipSize.Small,
        onClick = onClick,
    ) { contentColor ->
        TtcAvatar(initials = initials, color = avatarColor, size = AddPlayersSheetDefaults.ChipAvatarSize)
        Text(
            text = name,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            painter = TtcIcons.CLOSE.painter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(AddPlayersSheetDefaults.ChipRemoveIconSize),
        )
    }
}

private object AddPlayersSheetDefaults {
    /** The design's sheet takes about 88% of the screen's height. */
    const val HeightFraction = 0.88f
    val HeaderActionHeight = 48.dp
    val BodyPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 16.dp)
    val ContactsRowPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    val ContactsIconPadding = 9.dp
    val ContactsIconSize = 22.dp
    val RowSpacing = 12.dp
    val PromptTopPadding = 20.dp
    val FieldSpacing = 12.dp
    val MessageIconSpacing = 6.dp
    val MessageIconSize = 16.dp
    val TrayPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 20.dp)
    val TraySpacing = 12.dp
    val ChipSpacing = 8.dp
    val ChipAvatarSize = 24.dp
    val ChipRemoveIconSize = 16.dp
}

// `ModalBottomSheet` draws in its own window, which previews never rasterize (see `TtcBottomSheet`),
// so these preview the body on its own.

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AddPlayersSheetBodyPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            AddPlayersSheetBody(
                uiState = previewAddPlayersUiState,
                onChooseFromContacts = {},
                onNameChange = {},
                onPhoneChange = {},
                onAddEntered = {},
                onUnchoose = {},
                onAddChosen = {},
            )
        }
    }
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun AddPlayersSheetBodyEmptyPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            AddPlayersSheetBody(
                uiState = AddPlayersUiState.open(organizerPhone = "5025551234", invitees = emptyList()),
                onChooseFromContacts = {},
                onNameChange = {},
                onPhoneChange = {},
                onAddEntered = {},
                onUnchoose = {},
                onAddChosen = {},
            )
        }
    }
}
