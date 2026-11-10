package net.bradball.teetimecaddie.features.games.state.gamesListUiState

import net.bradball.teetimecaddie.core.extensions.formattedTime
import net.bradball.teetimecaddie.core.extensions.weekdayMonthDay
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.InviteStatus
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
 * @property status How full the game is, for the row's chip.
 * @property playerCount How many players are in: the organizer and everyone who said yes, e.g.
 *   "1 player" or "3 players". Invitees who haven't answered or said no don't count.
 * @property players The avatars of the players who are in, at most four: the organizer first,
 *   then the others in the order they said yes. Three at most when [overflow] adds a circle.
 * @property overflow The circle after [players]: "+" while the game is [GameFillStatus.OPEN], "+N"
 *   for the N invitees who haven't answered while it's [GameFillStatus.PENDING], and none once
 *   it's [GameFillStatus.FULL].
 */
data class GameRowUiState(
    val id: String,
    val course: String,
    val dateAndTime: LocalizedText,
    val status: GameFillStatus,
    val playerCount: LocalizedText,
    val players: List<PlayerAvatarUiState>,
    val overflow: PlayerStackOverflow
)

// The most circles a row's stack shows, the overflow circle included.
private const val MAX_STACK_CIRCLES = 4

internal fun Game.toRowUiState(): GameRowUiState {
    // A yes takes the next free slot, so slot order is the order people said yes. The sort is
    // stable: everyone but the organizer keeps it.
    val inGame = teeTimes.flatMap { it.players }.sortedBy { it.playerId != organizerId }
    val status = fillStatus
    val overflow = when (status) {
        GameFillStatus.OPEN -> PlayerStackOverflow.Add
        GameFillStatus.PENDING -> PlayerStackOverflow.More(invitees.count { it.status == InviteStatus.PENDING })
        GameFillStatus.FULL -> PlayerStackOverflow.None
    }
    val avatarRoom = if (overflow == PlayerStackOverflow.None) MAX_STACK_CIRCLES else MAX_STACK_CIRCLES - 1
    return GameRowUiState(
        id = id,
        course = course.name,
        dateAndTime = dateAndTime(),
        status = status,
        playerCount = if (inGame.size == 1) {
            LocalizedText(GMR.strings.games_row_player_count_one)
        } else {
            LocalizedText(GMR.strings.games_row_player_count_many, listOf(inGame.size.toString()))
        },
        players = inGame.take(avatarRoom).map { it.toAvatarUiState(organizerId) },
        overflow = overflow
    )
}

val previewGameRowUiState = previewGame.toRowUiState()

val previewGameRowUiStateList = previewGameList.map { it.toRowUiState() }
