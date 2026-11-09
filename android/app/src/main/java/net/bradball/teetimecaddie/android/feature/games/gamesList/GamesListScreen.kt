package net.bradball.teetimecaddie.android.feature.games.gamesList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.theme.TtcColorRole
import net.bradball.teetimecaddie.android.theme.toColorRole
import net.bradball.teetimecaddie.android.ui.common.ContentLoadingIndicator
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcAvatar
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcAvatarDefaults
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcPlayerAddSlot
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcPlayerItem
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcPlayerOverflow
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcPlayerStack
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcButton
import net.bradball.teetimecaddie.android.ui.common.cards.TtcCard
import net.bradball.teetimecaddie.android.ui.common.cards.TtcCardDefaults
import net.bradball.teetimecaddie.android.ui.common.chips.TtcChip
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcEmptyState
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.preview.ScreenPreviews
import net.bradball.teetimecaddie.android.ui.common.text.stringResource
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.features.auth.AR
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.GameFillStatus
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.GameRowUiState
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.GamesListUiState
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.InviteRowUiState
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.PlayerStackOverflow
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.previewGameRowUiStateList
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.previewInviteRowUiStateList

@Composable
fun GamesListScreen(
    onNewGameClick: () -> Unit,
    onGameClick: (String) -> Unit,
    viewModel: GamesListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Screen(AnalyticsScreen.Games("GamesListScreen")) {
        GamesListContent(
            uiState = uiState,
            onNewGameClick = { source ->
                viewModel.onAddGameClick(source)
                onNewGameClick()
            },
            onGameClick = { gameId ->
                viewModel.onGameClick()
                onGameClick(gameId)
            },
            onAnswerInvite = viewModel::onAnswerInvite,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GamesListContent(
    uiState: GamesListUiState,
    onNewGameClick: (AnalyticsEvent.AddGameClick.Source) -> Unit,
    onGameClick: (String) -> Unit,
    onAnswerInvite: (InviteRowUiState, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            // The brand wordmark rather than a screen title, as in the design.
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(AR.strings.auth_brand_name.resourceId),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                },
            )
        },
        floatingActionButton = {
            // Available in the empty state too, alongside its own "Create a game" button.
            FloatingActionButton(onClick = { onNewGameClick(AnalyticsEvent.AddGameClick.Source.ADD_BUTTON) }) {
                Icon(
                    painter = TtcIcons.ADD.painter,
                    contentDescription = stringResource(GMR.strings.games_new_game_action.resourceId),
                )
            }
        },
    ) { contentPadding ->
        when (uiState) {
            GamesListUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                ContentLoadingIndicator()
            }

            GamesListUiState.Empty -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(bottom = GamesListDefaults.EmptyStateFabClearance),
                contentAlignment = Alignment.Center,
            ) {
                TtcEmptyState(
                    title = stringResource(GMR.strings.games_empty_title.resourceId),
                    icon = TtcIcons.GOLF_COURSE,
                    description = stringResource(GMR.strings.games_empty_message.resourceId),
                ) {
                    TtcButton(
                        text = stringResource(GMR.strings.games_create_button.resourceId),
                        onClick = { onNewGameClick(AnalyticsEvent.AddGameClick.Source.EMPTY_LIST) },
                        icon = TtcIcons.ADD,
                    )
                }
            }

            is GamesListUiState.Content -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = GamesListDefaults.HorizontalPadding,
                    end = GamesListDefaults.HorizontalPadding,
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding() + GamesListDefaults.ListBottomPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(GamesListDefaults.Spacing),
            ) {
                if (uiState.invites.isNotEmpty()) {
                    item(key = "invites") {
                        InviteCard(uiState = uiState, onInviteClick = onGameClick, onAnswer = onAnswerInvite)
                    }
                }
                if (uiState.showsUpcomingHeader) {
                    item(key = "upcoming-header") {
                        Text(
                            text = stringResource(GMR.strings.games_upcoming_header.resourceId),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = GamesListDefaults.HeaderTopPadding),
                        )
                    }
                }
                items(uiState.games, key = { it.id }) { game ->
                    GameRow(uiState = game, onClick = { onGameClick(game.id) })
                }
            }
        }
    }
}

/**
 * A game on the Games tab. The top section has its course and its date and first tee time, with a
 * chip saying how full it is. Below a divider are how many players are in and their avatars.
 */
@Composable
private fun GameRow(uiState: GameRowUiState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TtcCard(modifier = modifier, onClick = onClick, contentPadding = TtcCardDefaults.NoContentPadding) {
        Row(
            modifier = Modifier.padding(GamesListDefaults.RowHeaderPadding),
            horizontalArrangement = Arrangement.spacedBy(GamesListDefaults.RowHeaderSpacing),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.course,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.padding(top = GamesListDefaults.RowLineSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(GamesListDefaults.RowIconSpacing),
                ) {
                    Icon(
                        painter = TtcIcons.SCHEDULE.painter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(GamesListDefaults.RowIconSize),
                    )
                    Text(
                        text = stringResource(uiState.dateAndTime),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            GameStatusChip(status = uiState.status)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier.padding(GamesListDefaults.RowFooterPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GamesListDefaults.PlayerCountIconSpacing),
            ) {
                Icon(
                    painter = TtcIcons.GROUP.painter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(GamesListDefaults.PlayerCountIconSize),
                )
                Text(
                    text = stringResource(uiState.playerCount),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TtcPlayerStack {
                uiState.players.forEach { player ->
                    TtcPlayerItem {
                        TtcAvatar(
                            initials = player.initial,
                            color = player.tone.toColorRole(),
                            size = TtcAvatarDefaults.SmallSize,
                        )
                    }
                }
                when (val overflow = uiState.overflow) {
                    PlayerStackOverflow.None -> Unit
                    PlayerStackOverflow.Add -> TtcPlayerAddSlot()
                    is PlayerStackOverflow.More -> TtcPlayerOverflow(text = overflow.label)
                }
            }
        }
    }
}

/** "Open", "Pending" or "Full", each in its own color with its own icon. */
@Composable
private fun GameStatusChip(status: GameFillStatus, modifier: Modifier = Modifier) {
    val (color, icon) = when (status) {
        GameFillStatus.OPEN -> TtcColorRole.Neutral to TtcIcons.PERSON_ADD
        GameFillStatus.PENDING -> TtcColorRole.Secondary to TtcIcons.HOURGLASS_EMPTY
        GameFillStatus.FULL -> TtcColorRole.Tertiary to TtcIcons.CHECK_CIRCLE
    }
    TtcChip(text = stringResource(status.label), modifier = modifier, color = color, icon = icon)
}

private object GamesListDefaults {
    val HorizontalPadding = 16.dp
    val Spacing = 16.dp
    val HeaderTopPadding = 4.dp
    /** Keeps the last row clear of the FAB. */
    val ListBottomPadding = 88.dp
    /** Centers the empty state in the space above the FAB, as the design does. */
    val EmptyStateFabClearance = 72.dp
    /** 22dp at the bottom: the design's 16dp card padding plus 6dp before the divider. */
    val RowHeaderPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 22.dp)
    val RowHeaderSpacing = 12.dp
    val RowLineSpacing = 4.dp
    val RowIconSpacing = 6.dp
    val RowIconSize = 16.dp
    val RowFooterPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    val PlayerCountIconSpacing = 8.dp
    val PlayerCountIconSize = 18.dp
}

@ScreenPreviews
@Composable
private fun GamesListContentPreview() {
    MyApplicationTheme {
        GamesListContent(
            uiState = GamesListUiState.Content(previewGameRowUiStateList, previewInviteRowUiStateList),
            onNewGameClick = {},
            onGameClick = {},
            onAnswerInvite = { _, _ -> },
        )
    }
}

@ScreenPreviews
@Composable
private fun GamesListInvitesOnlyPreview() {
    MyApplicationTheme {
        GamesListContent(
            uiState = GamesListUiState.Content(games = emptyList(), invites = previewInviteRowUiStateList.take(1)),
            onNewGameClick = {},
            onGameClick = {},
            onAnswerInvite = { _, _ -> },
        )
    }
}

@ScreenPreviews
@Composable
private fun GamesListEmptyPreview() {
    MyApplicationTheme {
        GamesListContent(
            uiState = GamesListUiState.Empty,
            onNewGameClick = {},
            onGameClick = {},
            onAnswerInvite = { _, _ -> },
        )
    }
}
