package net.bradball.teetimecaddie.android.feature.games.gameDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import net.bradball.teetimecaddie.android.ui.common.ContentLoadingIndicator
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcAvatar
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcAvatarPlaceholder
import net.bradball.teetimecaddie.android.ui.common.cards.TtcCard
import net.bradball.teetimecaddie.android.ui.common.cards.TtcCardDefaults
import net.bradball.teetimecaddie.android.ui.common.chips.TtcChip
import net.bradball.teetimecaddie.android.ui.common.chips.TtcChipSize
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcEmptyState
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.preview.ScreenPreviews
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.core.extensions.formattedDate
import net.bradball.teetimecaddie.core.extensions.formattedTime
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.GameTeeTime
import net.bradball.teetimecaddie.core.models.GR
import net.bradball.teetimecaddie.core.models.previewGame
import net.bradball.teetimecaddie.features.games.GMR

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameDetailsContent(
    uiState: GameDetailsUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(GMR.strings.game_details_title.resourceId)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = TtcIcons.ARROW_BACK.painter,
                            contentDescription = stringResource(GR.strings.back.resourceId),
                        )
                    }
                },
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
                    title = stringResource(uiState.title.resourceId),
                    description = stringResource(uiState.message.resourceId),
                )
            }

            is GameDetailsUiState.Content -> GameDetailsBody(
                game = uiState.game,
                isOrganizer = uiState.isOrganizer,
                modifier = contentModifier,
            )
        }
    }
}

/**
 * The game itself. Editing, adding tee times, inviting, managing, slot menus and the activity log
 * are all later work, and are left out rather than shown inert.
 */
@Composable
private fun GameDetailsBody(game: Game, isOrganizer: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(GameDetailsDefaults.Padding),
        verticalArrangement = Arrangement.spacedBy(GameDetailsDefaults.SectionSpacing),
    ) {
        if (isOrganizer) {
            TtcChip(
                text = stringResource(GMR.strings.game_details_organizer_chip.resourceId),
                color = TtcColorRole.Secondary,
                icon = TtcIcons.STAR_FILLED,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(GameDetailsDefaults.TitleSpacing)) {
            Text(
                text = game.course.name,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = game.date.formattedDate,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        game.teeTimes.forEach { teeTime ->
            TeeTimeCard(teeTime = teeTime, organizerId = game.organizerId)
        }
    }
}

@Composable
private fun TeeTimeCard(teeTime: GameTeeTime, organizerId: String, modifier: Modifier = Modifier) {
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
                text = teeTime.time.formattedTime,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            TtcChip(
                text = stringResource(
                    GMR.strings.game_details_filled_count.resourceId,
                    teeTime.filled.toString(),
                    teeTime.slots.toString(),
                ),
                size = TtcChipSize.Small,
            )
        }

        val rows = teeTime.players.map<GamePlayer, GamePlayer?> { it } + List(teeTime.openSlots) { null }
        rows.forEachIndexed { index, player ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = GameDetailsDefaults.DividerInset),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
            if (player == null) {
                EmptySlotRow()
            } else {
                PlayerSlotRow(player = player, isOrganizer = player.playerId == organizerId)
            }
        }
    }
}

@Composable
private fun PlayerSlotRow(player: GamePlayer, isOrganizer: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(GameDetailsDefaults.RowPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GameDetailsDefaults.RowSpacing),
    ) {
        TtcAvatar(
            initials = player.initial,
            color = if (isOrganizer) TtcColorRole.Secondary else TtcColorRole.Primary,
            size = GameDetailsDefaults.AvatarSize,
        )
        Column {
            Text(
                text = player.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            if (isOrganizer) {
                Text(
                    text = stringResource(GMR.strings.game_details_organizer_label.resourceId),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Composable
private fun EmptySlotRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(GameDetailsDefaults.RowPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GameDetailsDefaults.RowSpacing),
    ) {
        TtcAvatar(placeholder = TtcAvatarPlaceholder.Empty, size = GameDetailsDefaults.AvatarSize)
        Text(
            text = stringResource(GMR.strings.game_details_empty_slot.resourceId),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private object GameDetailsDefaults {
    val Padding = 16.dp
    val SectionSpacing = 20.dp
    val TitleSpacing = 8.dp
    val HeaderPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    val HeaderSpacing = 10.dp
    val HeaderIconSize = 20.dp
    val RowPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    val RowSpacing = 12.dp
    val AvatarSize = 36.dp
    /** Lines the dividers up with the row text, past the avatar. */
    val DividerInset = 64.dp
}

@ScreenPreviews
@Composable
private fun GameDetailsOrganizerPreview() {
    MyApplicationTheme {
        GameDetailsContent(
            uiState = GameDetailsUiState.Content(previewGame, isOrganizer = true),
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
                previewGame.copy(teeTimes = previewGame.teeTimes.map { it.copy(slots = 1) }),
                isOrganizer = true,
            ),
            onBack = {},
        )
    }
}
