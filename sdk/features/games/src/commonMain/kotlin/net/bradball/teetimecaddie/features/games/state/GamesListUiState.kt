package net.bradball.teetimecaddie.features.games.state

import net.bradball.teetimecaddie.core.extensions.formattedTime
import net.bradball.teetimecaddie.core.extensions.weekdayMonthDay
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.previewGame
import net.bradball.teetimecaddie.core.models.previewGameList
import net.bradball.teetimecaddie.features.games.GMR

/**
 * State for the Games tab.
 *
 * A sealed hierarchy rather than a data class, because the three cases never occur together: the
 * tab shows a spinner, the empty state, or the list.
 */
sealed interface GamesListUiState {
    data object Loading : GamesListUiState
    data object Empty : GamesListUiState
    data class Content(val games: List<GameRowUiState>) : GamesListUiState
}

/**
 * One game on the Games tab, already formatted.
 *
 * @property id The game's id, for opening its details.
 * @property course The course name.
 * @property dateAndTime The date and first tee time, e.g. "Sat, Jun 13 · 9:00 AM".
 */
data class GameRowUiState(
    val id: String,
    val course: String,
    val dateAndTime: LocalizedText
)

/** What the Games tab shows for these games: the empty state, or a row for each. */
fun List<Game>.toGamesListUiState(): GamesListUiState =
    if (isEmpty()) GamesListUiState.Empty else GamesListUiState.Content(map { it.toRowUiState() })

internal fun Game.toRowUiState(): GameRowUiState = GameRowUiState(
    id = id,
    course = course.name,
    dateAndTime = LocalizedText(
        GMR.strings.games_row_date_time,
        listOf(date.weekdayMonthDay, teeTimes.firstOrNull()?.time?.formattedTime.orEmpty())
    )
)

val previewGameRowUiState = previewGame.toRowUiState()

val previewGameRowUiStateList = previewGameList.map { it.toRowUiState() }
