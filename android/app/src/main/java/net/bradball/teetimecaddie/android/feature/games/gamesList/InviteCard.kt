package net.bradball.teetimecaddie.android.feature.games.gamesList

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.theme.toColorRole
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcAvatar
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcAvatarDefaults
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcPlayerItem
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcPlayerStack
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcButton
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcOutlinedButton
import net.bradball.teetimecaddie.android.ui.common.cards.TtcAccentCard
import net.bradball.teetimecaddie.android.ui.common.cards.TtcCardDefaults
import net.bradball.teetimecaddie.android.ui.common.chips.TtcChip
import net.bradball.teetimecaddie.android.ui.common.chips.TtcChipSize
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.text.stringResource
import net.bradball.teetimecaddie.core.models.GR
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.GamesListUiState
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.InviteRowUiState
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.previewInviteRowUiState
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.previewInviteRowUiStateList

/**
 * The Games tab's "New invite" card: every invitation waiting for the player's answer, a row each.
 *
 * Tapping a row opens that game. Yes and No answer right there, at once. A full game's Yes is
 * disabled, and its row carries a "Full" chip; No still works.
 *
 * @param uiState The tab's content, for its invitations and their header.
 * @param onInviteClick Called with the game's id when a row is tapped.
 * @param onAnswer Called with the invitation and whether the answer was Yes.
 */
@Composable
fun InviteCard(
    uiState: GamesListUiState.Content,
    onInviteClick: (String) -> Unit,
    onAnswer: (InviteRowUiState, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    TtcAccentCard(modifier = modifier, contentPadding = TtcCardDefaults.NoContentPadding) {
        Row(
            modifier = Modifier.padding(InviteCardDefaults.HeaderPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(InviteCardDefaults.HeaderIconSpacing),
        ) {
            Icon(
                painter = TtcIcons.MARK_EMAIL_UNREAD.painter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(InviteCardDefaults.HeaderIconSize),
            )
            Text(
                text = stringResource(uiState.invitesHeader).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        uiState.invites.forEachIndexed { index, invite ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = InviteCardDefaults.StartPadding),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
            InviteRow(
                uiState = invite,
                onClick = { onInviteClick(invite.id) },
                onAnswer = { accept -> onAnswer(invite, accept) },
            )
        }
    }
}

@Composable
private fun InviteRow(
    uiState: InviteRowUiState,
    onClick: () -> Unit,
    onAnswer: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(InviteCardDefaults.RowPadding),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(InviteCardDefaults.ChipSpacing),
        ) {
            Text(
                text = uiState.course,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (uiState.isFull) {
                TtcChip(
                    text = stringResource(GMR.strings.games_invite_full_chip.resourceId),
                    size = TtcChipSize.Small,
                )
            }
        }
        Row(
            modifier = Modifier.padding(top = InviteCardDefaults.LineSpacing),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(InviteCardDefaults.DateIconSpacing),
        ) {
            Icon(
                painter = TtcIcons.CALENDAR.painter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(InviteCardDefaults.DateIconSize),
            )
            Text(
                text = stringResource(uiState.dateAndTime),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.padding(top = InviteCardDefaults.ActionsTopPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(InviteCardDefaults.ActionSpacing),
        ) {
            TtcPlayerStack(modifier = Modifier.weight(1f)) {
                uiState.players.forEach { player ->
                    TtcPlayerItem {
                        TtcAvatar(
                            initials = player.initial,
                            color = player.tone.toColorRole(),
                            size = TtcAvatarDefaults.SmallSize,
                        )
                    }
                }
            }
            TtcOutlinedButton(
                text = stringResource(GR.strings.no.resourceId),
                onClick = { onAnswer(false) },
                dense = true,
            )
            TtcButton(
                text = stringResource(GR.strings.yes.resourceId),
                onClick = { onAnswer(true) },
                dense = true,
                enabled = !uiState.isFull,
            )
        }
    }
}

private object InviteCardDefaults {
    /** Content starts this far in from the accent rail, rows and dividers alike. */
    val StartPadding = 18.dp
    val HeaderPadding = PaddingValues(start = StartPadding, top = 14.dp, end = 16.dp, bottom = 6.dp)
    val HeaderIconSize = 16.dp
    val HeaderIconSpacing = 6.dp
    val RowPadding = PaddingValues(start = StartPadding, top = 12.dp, end = 16.dp, bottom = 14.dp)
    val ChipSpacing = 8.dp
    val LineSpacing = 2.dp
    val DateIconSize = 16.dp
    val DateIconSpacing = 6.dp
    val ActionsTopPadding = 10.dp
    val ActionSpacing = 8.dp
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun InviteCardPreview() {
    MyApplicationTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                InviteCard(
                    uiState = GamesListUiState.Content(games = emptyList(), invites = listOf(previewInviteRowUiState)),
                    onInviteClick = {},
                    onAnswer = { _, _ -> },
                )
                InviteCard(
                    uiState = GamesListUiState.Content(games = emptyList(), invites = previewInviteRowUiStateList),
                    onInviteClick = {},
                    onAnswer = { _, _ -> },
                )
            }
        }
    }
}
