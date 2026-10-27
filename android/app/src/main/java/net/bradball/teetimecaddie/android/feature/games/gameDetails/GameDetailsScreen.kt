package net.bradball.teetimecaddie.android.feature.games.gameDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.theme.TtcColorRole
import net.bradball.teetimecaddie.android.ui.common.ContentLoadingIndicator
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.appBars.TtcCenteredTopAppBar
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcAvatar
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcAvatarPlaceholder
import net.bradball.teetimecaddie.android.ui.common.cards.TtcCard
import net.bradball.teetimecaddie.android.ui.common.cards.TtcCardDefaults
import net.bradball.teetimecaddie.android.ui.common.chips.TtcChip
import net.bradball.teetimecaddie.android.ui.common.chips.TtcChipSize
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcEmptyState
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.preview.ScreenPreviews
import net.bradball.teetimecaddie.android.ui.common.text.stringResource
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.core.models.previewGame
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.state.GameUiState
import net.bradball.teetimecaddie.features.games.state.GameDetailsUiState
import net.bradball.teetimecaddie.features.games.state.GameInviteeSlotUiState
import net.bradball.teetimecaddie.features.games.state.GameSlotUiState
import net.bradball.teetimecaddie.features.games.state.GameTeeTimeUiState
import net.bradball.teetimecaddie.features.games.state.previewGameUiState
import net.bradball.teetimecaddie.features.games.state.previewGameWithInviteesUiState
import net.bradball.teetimecaddie.features.games.state.toUiState

@Composable
fun GameDetailsScreen(
    viewModel: GameDetailsViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Screen(AnalyticsScreen.GameDetails("GameDetailsScreen")) {
        GameDetailsContent(uiState = uiState, onBack = onBack)
    }
}

@Composable
private fun GameDetailsContent(
    uiState: GameDetailsUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TtcCenteredTopAppBar(
                title = stringResource(GMR.strings.game_details_title.resourceId),
                onBack = onBack,
            )
        },
    ) { contentPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)

        when (uiState) {
            GameDetailsUiState.Loading -> Box(contentModifier) {
                ContentLoadingIndicator()
            }

            is GameDetailsUiState.Error -> Box(contentModifier, contentAlignment = Alignment.Center) {
                TtcEmptyState(
                    title = stringResource(uiState.title),
                    description = stringResource(uiState.message),
                )
            }

            is GameDetailsUiState.Content -> GameDetailsBody(uiState = uiState.game, modifier = contentModifier)
        }
    }
}

/**
 * The game itself. Editing, adding tee times, inviting, managing, slot menus and the activity log
 * are all later work, and are left out rather than shown inert.
 */
@Composable
private fun GameDetailsBody(uiState: GameUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(GameDetailsDefaults.Padding),
        verticalArrangement = Arrangement.spacedBy(GameDetailsDefaults.SectionSpacing),
    ) {
        if (uiState.isOrganizer) {
            TtcChip(
                text = stringResource(GMR.strings.game_details_organizer_chip.resourceId),
                color = TtcColorRole.Secondary,
                icon = TtcIcons.STAR_FILLED,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(GameDetailsDefaults.TitleSpacing)) {
            Text(
                text = uiState.course,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = uiState.date,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        uiState.teeTimes.forEach { teeTime ->
            TeeTimeCard(uiState = teeTime)
        }
    }
}

@Composable
private fun TeeTimeCard(uiState: GameTeeTimeUiState, modifier: Modifier = Modifier) {
    TtcCard(modifier = modifier, contentPadding = TtcCardDefaults.NoContentPadding) {
        // The tonal header strip: when, and how full.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(GameDetailsDefaults.HeaderPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameDetailsDefaults.HeaderSpacing),
        ) {
            Icon(
                painter = TtcIcons.SCHEDULE.painter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(GameDetailsDefaults.HeaderIconSize),
            )
            Text(
                text = uiState.time,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            TtcChip(
                text = stringResource(uiState.filledCount),
                size = TtcChipSize.Small,
            )
        }

        // Players, then invitees, then the empty slots that are left. Every row but the last
        // carries a divider.
        val inviteesStart = uiState.players.size
        val emptyStart = inviteesStart + uiState.invitees.size
        val lastRow = emptyStart + uiState.openSlots - 1
        uiState.players.forEachIndexed { i, player ->
            PlayerSlotRow(uiState = player, showDivider = i < lastRow)
        }
        uiState.invitees.forEachIndexed { i, invitee ->
            InviteeSlotRow(uiState = invitee, showDivider = inviteesStart + i < lastRow)
        }
        repeat(uiState.openSlots) { i ->
            EmptySlotRow(showDivider = emptyStart + i < lastRow)
        }
    }
}

@Composable
private fun PlayerSlotRow(
    uiState: GameSlotUiState,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    SlotRow(
        showDivider = showDivider,
        modifier = modifier,
        avatar = {
            TtcAvatar(
                initials = uiState.initial,
                color = if (uiState.isOrganizer) TtcColorRole.Secondary else TtcColorRole.Primary,
                size = GameDetailsDefaults.AvatarSize,
            )
        },
    ) {
        Text(
            text = uiState.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
        if (uiState.isOrganizer) {
            Text(
                text = stringResource(GMR.strings.game_details_organizer_label.resourceId),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun InviteeSlotRow(
    uiState: GameInviteeSlotUiState,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    SlotRow(
        showDivider = showDivider,
        modifier = modifier,
        avatar = {
            TtcAvatar(
                initials = uiState.initial,
                color = TtcColorRole.Primary,
                size = GameDetailsDefaults.AvatarSize,
            )
        },
    ) {
        Text(
            text = uiState.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(GMR.strings.game_details_invited_label.resourceId),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptySlotRow(showDivider: Boolean, modifier: Modifier = Modifier) {
    SlotRow(
        showDivider = showDivider,
        modifier = modifier,
        avatar = { TtcAvatar(placeholder = TtcAvatarPlaceholder.Empty, size = GameDetailsDefaults.AvatarSize) },
    ) {
        Text(
            text = stringResource(GMR.strings.game_details_empty_slot.resourceId),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * The layout every slot row shares: the avatar, then the text with an optional divider beneath it.
 *
 * The row's vertical padding sits inside the text column rather than on the row, so the divider runs
 * along the row's bottom edge and starts exactly where the text does, with no inset to keep in sync.
 */
@Composable
private fun SlotRow(
    showDivider: Boolean,
    avatar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    text: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = GameDetailsDefaults.RowHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GameDetailsDefaults.RowSpacing),
    ) {
        avatar()
        Column(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.padding(
                    top = GameDetailsDefaults.RowVerticalPadding,
                    bottom = GameDetailsDefaults.RowVerticalPadding,
                    end = GameDetailsDefaults.RowHorizontalPadding,
                ),
                content = text,
            )
            if (showDivider) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

private object GameDetailsDefaults {
    val Padding = 16.dp
    val SectionSpacing = 20.dp
    val TitleSpacing = 8.dp
    val HeaderPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    val HeaderSpacing = 10.dp
    val HeaderIconSize = 20.dp
    val RowHorizontalPadding = 16.dp
    val RowVerticalPadding = 12.dp
    val RowSpacing = 12.dp
    val AvatarSize = 36.dp
}

@ScreenPreviews
@Composable
private fun GameDetailsOrganizerPreview() {
    MyApplicationTheme {
        GameDetailsContent(
            uiState = GameDetailsUiState.Content(previewGameUiState),
            onBack = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun GameDetailsWithInviteesPreview() {
    MyApplicationTheme {
        GameDetailsContent(
            uiState = GameDetailsUiState.Content(previewGameWithInviteesUiState),
            onBack = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun GameDetailsSingleSlotPreview() {
    MyApplicationTheme {
        GameDetailsContent(
            uiState = GameDetailsUiState.Content(
                previewGame
                    .copy(teeTimes = previewGame.teeTimes.map { it.copy(slots = 1) })
                    .toUiState(viewerId = previewGame.organizerId)
            ),
            onBack = {},
        )
    }
}
