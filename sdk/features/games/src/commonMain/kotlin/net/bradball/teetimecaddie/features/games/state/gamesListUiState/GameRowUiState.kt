package net.bradball.teetimecaddie.features.games.state.gamesListUiState

import net.bradball.teetimecaddie.core.extensions.formattedTime
import net.bradball.teetimecaddie.core.extensions.weekdayMonthDay
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.previewGame
import net.bradball.teetimecaddie.core.models.previewGameList
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.dateAndTime

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

internal fun Game.toRowUiState(): GameRowUiState = GameRowUiState(
    id = id,
    course = course.name,
    dateAndTime = dateAndTime()
)

val previewGameRowUiState = previewGame.toRowUiState()

val previewGameRowUiStateList = previewGameList.map { it.toRowUiState() }
