package net.bradball.teetimecaddie.features.games.state

import net.bradball.teetimecaddie.core.extensions.formattedTime
import net.bradball.teetimecaddie.core.extensions.weekdayMonthDay
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException
import net.bradball.teetimecaddie.core.models.previewGame
import net.bradball.teetimecaddie.features.games.GMR

/** State for a game's details. */
sealed interface GameDetailsUiState {
    data object Loading : GameDetailsUiState

    data class Content(val game: GameDetailsUiModel) : GameDetailsUiState

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
data class GameDetailsUiModel(
    val course: String,
    val date: String,
    val isOrganizer: Boolean,
    val teeTimes: List<GameTeeTimeUiModel>
)

/**
 * One tee time, formatted.
 *
 * @property time When the group tees off, e.g. "9:00 AM".
 * @property filledCount How full it is, e.g. "1/4 filled".
 * @property players The occupied slots, in slot order.
 * @property openSlots How many empty slots follow [players].
 */
data class GameTeeTimeUiModel(
    val time: String,
    val filledCount: LocalizedText,
    val players: List<GameSlotUiModel>,
    val openSlots: Int
)

/**
 * A player in one of a tee time's slots.
 *
 * @property initial The letter in the player's avatar.
 * @property isOrganizer Whether this player organized the game, which labels the slot "Organizer".
 */
data class GameSlotUiModel(
    val initial: String,
    val name: String,
    val isOrganizer: Boolean
)

/** This game's details as [viewerId] sees them. A null [viewerId] is never the organizer. */
fun Game.toDetailsUiModel(viewerId: String?): GameDetailsUiModel = GameDetailsUiModel(
    course = course.name,
    date = date.weekdayMonthDay,
    isOrganizer = viewerId != null && isOrganizer(viewerId),
    teeTimes = teeTimes.map { teeTime ->
        GameTeeTimeUiModel(
            time = teeTime.time.formattedTime,
            filledCount = LocalizedText(
                GMR.strings.game_details_filled_count,
                listOf(teeTime.filled.toString(), teeTime.slots.toString())
            ),
            players = teeTime.players.map { player ->
                GameSlotUiModel(
                    initial = player.initial,
                    name = player.name,
                    isOrganizer = player.playerId == organizerId
                )
            },
            openSlots = teeTime.openSlots
        )
    }
)

/** [previewGame] as its organizer sees it: one player and three open slots. */
val previewGameDetailsUiModel = previewGame.toDetailsUiModel(viewerId = previewGame.organizerId)
