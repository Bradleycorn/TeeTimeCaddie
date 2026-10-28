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
 * @property players One row per person on the tee time's roster: those holding a slot, in slot
 *   order, then those invited and not yet confirmed, in the order they were added. Never capped:
 *   there can be more rows than slots.
 * @property openSlots How many empty slots follow [players]: the slots no row has used, and never
 *   below zero.
 */
data class GameTeeTimeUiState(
    val time: String,
    val filledCount: LocalizedText,
    val players: List<TeeTimePlayerRowUiState>,
    val openSlots: Int
)

/** Where a person on a tee time's roster stands, which decides how their row is labelled. */
enum class TeeTimePlayerStatus {
    /** Organized the game, and holds a slot. Labelled "Organizer". */
    ORGANIZER,

    /** Holds a slot. Shown without a label. */
    CONFIRMED,

    /** Invited, and hasn't taken a slot. Labelled "Invited". */
    INVITED
}

/**
 * One row of a tee time's roster.
 *
 * @property initial The letter in the person's avatar.
 * @property status Where they stand, which decides the row's label and avatar color.
 */
data class TeeTimePlayerRowUiState(
    val initial: String,
    val name: String,
    val status: TeeTimePlayerStatus
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
        val slotHolders = teeTime.players.map { player ->
            TeeTimePlayerRowUiState(
                initial = player.initial,
                name = player.name,
                status = if (player.playerId == organizerId) {
                    TeeTimePlayerStatus.ORGANIZER
                } else {
                    TeeTimePlayerStatus.CONFIRMED
                }
            )
        }
        val invited = if (index == 0) {
            invitees.map {
                TeeTimePlayerRowUiState(initial = it.initial, name = it.name, status = TeeTimePlayerStatus.INVITED)
            }
        } else {
            emptyList()
        }
        GameTeeTimeUiState(
            time = teeTime.time.formattedTime,
            filledCount = LocalizedText(
                GMR.strings.game_details_filled_count,
                listOf(teeTime.filled.toString(), teeTime.slots.toString())
            ),
            players = slotHolders + invited,
            openSlots = (teeTime.openSlots - invited.size).coerceAtLeast(0)
        )
    }
)

/** [previewGame] as its organizer sees it: one player and three open slots. */
val previewGameUiState = previewGame.toUiState(viewerId = previewGame.organizerId)

/** [previewGame] with three invitees, as its organizer sees it: no open slots left to show. */
val previewGameWithInviteesUiState = previewGame
    .copy(invitees = previewGameInviteeList)
    .toUiState(viewerId = previewGame.organizerId)
