package net.bradball.teetimecaddie.android.feature.games.gameDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.IconButton
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
import net.bradball.teetimecaddie.android.theme.toColorRole
import net.bradball.teetimecaddie.android.ui.common.ContentLoadingIndicator
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.appBars.TtcCenteredTopAppBar
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcAvatar
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcAvatarDefaults
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
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.previewGame
import net.bradball.teetimecaddie.core.models.previewPlayer
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.DeclinedInviteeUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.GameUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.GameDetailsUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.GameTeeTimeUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.TeeTimePlayerRowUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.TeeTimePlayerStatus
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.previewGameUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.previewGameWithInviteesUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.previewAcceptedGameUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.previewInvitedGameUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.toDetailsUiState

@Composable
fun GameDetailsScreen(
    viewModel: GameDetailsViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Screen(AnalyticsScreen.GameDetails("GameDetailsScreen")) {
        GameDetailsContent(
            uiState = uiState,
            onBack = onBack,
            onAnswer = viewModel::onAnswer,
            onInviteeClick = viewModel::onInviteeClick,
            onRecordResponse = viewModel::onRecordResponse,
            onResponseSheetDismiss = viewModel::onResponseSheetDismiss,
        )
    }
}

@Composable
private fun GameDetailsContent(
    uiState: GameDetailsUiState,
    onBack: () -> Unit,
    onAnswer: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onInviteeClick: (String) -> Unit = {},
    onRecordResponse: (Boolean) -> Unit = {},
    onResponseSheetDismiss: () -> Unit = {},
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

            is GameDetailsUiState.Content -> {
                GameDetailsBody(
                    uiState = uiState.game,
                    onAnswer = onAnswer,
                    onInviteeClick = onInviteeClick,
                    modifier = contentModifier,
                )
                (uiState.game as? GameUiState.Organizer)?.responseSheet?.let { sheet ->
                    ResponseSheet(
                        uiState = sheet,
                        onRecordResponse = onRecordResponse,
                        onDismiss = onResponseSheetDismiss,
                    )
                }
            }
        }
    }
}

/**
 * The game itself, in the organizer's view or an invitee's. Editing, adding tee times, inviting,
 * managing, menus on confirmed and declined rows, and the activity log are all later work, and are
 * left out rather than shown inert.
 */
@Composable
private fun GameDetailsBody(
    uiState: GameUiState,
    onAnswer: (Boolean) -> Unit,
    onInviteeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(GameDetailsDefaults.Padding),
        verticalArrangement = Arrangement.spacedBy(GameDetailsDefaults.SectionSpacing),
    ) {
        when (uiState) {
            is GameUiState.Organizer -> OrganizerHeader(uiState)
            is GameUiState.Invitee -> InviteeHeader(uiState, onAnswer = onAnswer)
        }

        uiState.teeTimes.forEach { teeTime ->
            TeeTimeCard(uiState = teeTime, onInviteeClick = onInviteeClick)
        }

        if (uiState is GameUiState.Organizer && uiState.declined.isNotEmpty()) {
            DeclinedSection(heading = uiState.declinedHeading, declined = uiState.declined)
        }
    }
}

/** The "You are the organizer" chip, the course and the date. */
@Composable
private fun OrganizerHeader(uiState: GameUiState.Organizer) {
    TtcChip(
        text = stringResource(GMR.strings.game_details_organizer_chip.resourceId),
        color = TtcColorRole.Secondary,
        icon = TtcIcons.STAR_FILLED,
    )
    GameTitle(course = uiState.course, subtitle = uiState.date)
}

/** The course, who organized the game, and the RSVP card when there is an invitation to show. */
@Composable
private fun InviteeHeader(uiState: GameUiState.Invitee, onAnswer: (Boolean) -> Unit) {
    GameTitle(
        course = uiState.course,
        subtitle = uiState.organizedBy?.let { stringResource(it) } ?: uiState.date,
    )
    uiState.rsvp?.let { RsvpCard(uiState = it, onAnswer = onAnswer) }
}

@Composable
private fun GameTitle(course: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(GameDetailsDefaults.TitleSpacing)) {
        Text(
            text = course,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TeeTimeCard(
    uiState: GameTeeTimeUiState,
    onInviteeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
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

        // The roster, then the empty slots that are left. Every row but the last carries a divider.
        val lastRow = uiState.players.size + uiState.openSlots - 1
        uiState.players.forEachIndexed { i, player ->
            PlayerRow(uiState = player, showDivider = i < lastRow, onInviteeClick = onInviteeClick)
        }
        repeat(uiState.openSlots) { i ->
            EmptySlotRow(showDivider = uiState.players.size + i < lastRow)
        }
    }
}

/**
 * One person on the roster. In the organizer's view, an invitee who hasn't answered can be tapped,
 * or their ⋮, to record their answer.
 */
@Composable
private fun PlayerRow(
    uiState: TeeTimePlayerRowUiState,
    showDivider: Boolean,
    onInviteeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val inviteePhone = uiState.inviteePhone
    SlotRow(
        showDivider = showDivider,
        modifier = if (inviteePhone != null) {
            modifier.clickable { onInviteeClick(inviteePhone) }
        } else {
            modifier
        },
        trailing = inviteePhone?.let { phone ->
            {
                IconButton(onClick = { onInviteeClick(phone) }) {
                    Icon(
                        painter = TtcIcons.MORE_VERT.painter,
                        contentDescription = stringResource(
                            LocalizedText(GMR.strings.game_details_response_more, listOf(uiState.name))
                        ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        avatar = {
            TtcAvatar(
                initials = uiState.initial,
                color = uiState.tone.toColorRole(),
                size = GameDetailsDefaults.AvatarSize,
            )
        },
    ) {
        Text(
            text = uiState.viewerName?.let { stringResource(it) } ?: uiState.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
        when (uiState.status) {
            TeeTimePlayerStatus.ORGANIZER -> Text(
                text = stringResource(GMR.strings.game_details_organizer_label.resourceId),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            TeeTimePlayerStatus.INVITED -> Text(
                text = stringResource(GMR.strings.game_details_invited_label.resourceId),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TeeTimePlayerStatus.CONFIRMED -> Text(
                text = stringResource(GMR.strings.game_details_confirmed_label.resourceId),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

/** Who said no, under a "Declined · N" heading. The organizer's view only. */
@Composable
private fun DeclinedSection(
    heading: LocalizedText,
    declined: List<DeclinedInviteeUiState>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(GameDetailsDefaults.DeclinedHeadingSpacing),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = GameDetailsDefaults.DeclinedHeadingInset),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GameDetailsDefaults.DeclinedHeadingSpacing),
        ) {
            Icon(
                painter = TtcIcons.PERSON_OFF.painter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(GameDetailsDefaults.DeclinedIconSize),
            )
            Text(
                text = stringResource(heading),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TtcCard(color = TtcColorRole.Subtle, contentPadding = GameDetailsDefaults.DeclinedListPadding) {
            declined.forEach { DeclinedRow(uiState = it) }
        }
    }
}

@Composable
private fun DeclinedRow(uiState: DeclinedInviteeUiState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(GameDetailsDefaults.DeclinedRowPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GameDetailsDefaults.RowSpacing),
    ) {
        TtcAvatar(
            initials = uiState.initial,
            color = TtcColorRole.Neutral,
            size = TtcAvatarDefaults.SmallSize,
        )
        Text(
            text = uiState.name,
            style = MaterialTheme.typography.bodyMedium,
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
 * The layout every slot row shares: the avatar, then the text and an optional [trailing] control,
 * with an optional divider beneath them.
 *
 * The row's vertical padding sits inside the text column rather than on the row, so the divider runs
 * along the row's bottom edge and starts exactly where the text does, with no inset to keep in sync.
 */
@Composable
private fun SlotRow(
    showDivider: Boolean,
    avatar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            top = GameDetailsDefaults.RowVerticalPadding,
                            bottom = GameDetailsDefaults.RowVerticalPadding,
                            end = if (trailing == null) GameDetailsDefaults.RowHorizontalPadding else 0.dp,
                        ),
                    content = text,
                )
                if (trailing != null) {
                    Box(modifier = Modifier.padding(end = GameDetailsDefaults.TrailingEndPadding)) { trailing() }
                }
            }
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
    val TrailingEndPadding = 4.dp
    val AvatarSize = 36.dp
    val DeclinedHeadingInset = 4.dp
    val DeclinedHeadingSpacing = 8.dp
    val DeclinedIconSize = 18.dp
    val DeclinedListPadding = PaddingValues(vertical = 4.dp)
    val DeclinedRowPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
}

@ScreenPreviews
@Composable
private fun GameDetailsOrganizerPreview() {
    MyApplicationTheme {
        GameDetailsContent(
            uiState = GameDetailsUiState.Content(previewGameUiState),
            onBack = {},
            onAnswer = {},
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
            onAnswer = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun GameDetailsSingleSlotPreview() {
    MyApplicationTheme {
        GameDetailsContent(
            uiState = previewGame
                .copy(teeTimes = previewGame.teeTimes.map { it.copy(slots = 1) })
                .toDetailsUiState(viewer = previewPlayer),
            onBack = {},
            onAnswer = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun GameDetailsInviteePreview() {
    MyApplicationTheme {
        GameDetailsContent(
            uiState = GameDetailsUiState.Content(previewInvitedGameUiState),
            onBack = {},
            onAnswer = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun GameDetailsAcceptedPreview() {
    MyApplicationTheme {
        GameDetailsContent(
            uiState = GameDetailsUiState.Content(previewAcceptedGameUiState),
            onBack = {},
            onAnswer = {},
        )
    }
}
