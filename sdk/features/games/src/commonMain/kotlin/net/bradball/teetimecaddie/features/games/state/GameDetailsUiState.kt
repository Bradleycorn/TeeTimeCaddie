package net.bradball.teetimecaddie.features.games.state

import net.bradball.teetimecaddie.core.extensions.formattedTime
import net.bradball.teetimecaddie.core.extensions.weekdayMonthDay
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException
import net.bradball.teetimecaddie.core.models.previewGame
import net.bradball.teetimecaddie.core.models.previewGameInviteeList
import net.bradball.teetimecaddie.features.games.GMR

/** State for a game's details. */
sealed interface GameDetailsUiState {
    data object Loading : GameDetailsUiState

    data class Content(val game: GameUiState) : GameDetailsUiState

    /** The game could not be read, or no longer exists. */
    data class Error(val title: LocalizedText, val message: LocalizedText) : GameDetailsUiState {
        /** The failure's own title and message, with the message's placeholders filled. */
        constructor(error: TeeTimeCaddieException) : this(
            title = LocalizedText(error.title),
            message = LocalizedText(error.displayMessage, error.messageArgs)
        )
    }
}

/**
 * A game's details, already formatted.
 *
 * @property course The course name.
 * @property date The game's date, e.g. "Sat, Jun 13".
 * @property isOrganizer Whether the signed-in player organized the game, which decides the
 *   "You are the organizer" chip.
 * @property teeTimes The game's tee times, earliest first.
 */
data class GameUiState(
    val course: String,
    val date: String,
    val isOrganizer: Boolean,
    val teeTimes: List<GameTeeTimeUiState>
)

/**
 * One tee time, formatted.
 *
 * @property time When the group tees off, e.g. "9:00 AM".
 * @property filledCount How full it is, e.g. "1/4 filled".
 * @property players The occupied slots, in slot order.
 * @property invitees The people invited but not yet holding a slot, listed after [players] in the
 *   order they were added. Never capped: there can be more of them than open slots.
 * @property openSlots How many empty slots follow the invitees: the open slots left once each
 *   invitee is given a row, and never below zero.
 */
data class GameTeeTimeUiState(
    val time: String,
    val filledCount: LocalizedText,
    val players: List<GameSlotUiState>,
    val invitees: List<GameInviteeSlotUiState>,
    val openSlots: Int
)

/**
 * A player in one of a tee time's slots.
 *
 * @property initial The letter in the player's avatar.
 * @property isOrganizer Whether this player organized the game, which labels the slot "Organizer".
 */
data class GameSlotUiState(
    val initial: String,
    val name: String,
    val isOrganizer: Boolean
)

/**
 * Someone invited to the game who hasn't responded, shown on a row labelled "Invited".
 *
 * @property initial The letter in the invitee's avatar.
 */
data class GameInviteeSlotUiState(
    val initial: String,
    val name: String
)

/**
 * This game's details as [viewerId] sees them. A null [viewerId] is never the organizer.
 *
 * Invitees belong to the game rather than to a tee time, so they are listed under the first tee
 * time — for now every game has exactly one. They don't fill a slot until they say yes, so the
 * filled count still counts only the tee time's players.
 */
fun Game.toUiState(viewerId: String?): GameUiState = GameUiState(
    course = course.name,
    date = date.weekdayMonthDay,
    isOrganizer = viewerId != null && isOrganizer(viewerId),
    teeTimes = teeTimes.mapIndexed { index, teeTime ->
        val invitees = if (index == 0) {
            invitees.map { GameInviteeSlotUiState(initial = it.initial, name = it.name) }
        } else {
            emptyList()
        }
        GameTeeTimeUiState(
            time = teeTime.time.formattedTime,
            filledCount = LocalizedText(
                GMR.strings.game_details_filled_count,
                listOf(teeTime.filled.toString(), teeTime.slots.toString())
            ),
            players = teeTime.players.map { player ->
                GameSlotUiState(
                    initial = player.initial,
                    name = player.name,
                    isOrganizer = player.playerId == organizerId
                )
            },
            invitees = invitees,
            openSlots = (teeTime.openSlots - invitees.size).coerceAtLeast(0)
        )
    }
)

/** [previewGame] as its organizer sees it: one player and three open slots. */
val previewGameUiState = previewGame.toUiState(viewerId = previewGame.organizerId)

/** [previewGame] with three invitees, as its organizer sees it: no open slots left to show. */
val previewGameWithInviteesUiState = previewGame
    .copy(invitees = previewGameInviteeList)
    .toUiState(viewerId = previewGame.organizerId)
