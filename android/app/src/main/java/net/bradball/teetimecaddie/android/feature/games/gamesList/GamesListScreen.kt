package net.bradball.teetimecaddie.android.feature.games.gamesList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import net.bradball.teetimecaddie.android.ui.common.ContentLoadingIndicator
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcButton
import net.bradball.teetimecaddie.android.ui.common.cards.TtcCard
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcEmptyState
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.preview.ScreenPreviews
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.core.extensions.weekdayMonthDay
import net.bradball.teetimecaddie.core.extensions.formattedTime
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.previewGameList
import net.bradball.teetimecaddie.features.auth.AR
import net.bradball.teetimecaddie.features.games.GMR

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
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GamesListContent(
    uiState: GamesListUiState,
    onNewGameClick: (AnalyticsEvent.AddGameClick.Source) -> Unit,
    onGameClick: (String) -> Unit,
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
                item {
                    Text(
                        text = stringResource(GMR.strings.games_upcoming_header.resourceId),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = GamesListDefaults.HeaderTopPadding),
                    )
                }
                items(uiState.games, key = { it.id }) { game ->
                    GameRow(game = game, onClick = { onGameClick(game.id) })
                }
            }
        }
    }
}

/**
 * A game on the Games tab: its course, then its date and first tee time.
 *
 * The status chip, player count and avatar stack in the design arrive with later stories.
 */
@Composable
private fun GameRow(game: Game, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TtcCard(modifier = modifier, onClick = onClick) {
        Text(
            text = game.course.name,
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
                text = stringResource(
                    GMR.strings.games_row_date_time.resourceId,
                    game.date.weekdayMonthDay,
                    game.teeTimes.firstOrNull()?.time?.formattedTime.orEmpty(),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private object GamesListDefaults {
    val HorizontalPadding = 16.dp
    val Spacing = 16.dp
    val HeaderTopPadding = 4.dp
    /** Keeps the last row clear of the FAB. */
    val ListBottomPadding = 88.dp
    /** Centers the empty state in the space above the FAB, as the design does. */
    val EmptyStateFabClearance = 72.dp
    val RowLineSpacing = 4.dp
    val RowIconSpacing = 6.dp
    val RowIconSize = 16.dp
}

@ScreenPreviews
@Composable
private fun GamesListContentPreview() {
    MyApplicationTheme {
        GamesListContent(
            uiState = GamesListUiState.Content(previewGameList),
            onNewGameClick = {},
            onGameClick = {},
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
        )
    }
}
