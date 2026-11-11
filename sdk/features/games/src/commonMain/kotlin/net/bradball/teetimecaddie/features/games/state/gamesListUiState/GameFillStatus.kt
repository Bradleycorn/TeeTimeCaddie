package net.bradball.teetimecaddie.features.games.state.gamesListUiState

import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.features.games.GMR

/** How full a game is, as its Games-tab row's chip shows it. */
enum class GameFillStatus {
    /** At least one slot is held by neither a confirmed player nor an invitee who hasn't answered. Labelled "Open". */
    OPEN,

    /** Every free slot could go to an invitee who hasn't answered yet. Labelled "Pending". */
    PENDING,

    /**
     * Confirmed players hold every slot. Labelled "Full", even while some invitees haven't
     * answered: there's no slot left for them.
     */
    FULL;

    /** The chip's text: "Open", "Pending" or "Full". */
    val label: LocalizedText
        get() = LocalizedText(
            when (this) {
                OPEN -> GMR.strings.games_status_open
                PENDING -> GMR.strings.games_status_pending
                FULL -> GMR.strings.games_status_full
            }
        )
}

/**
 * Whether confirmed players hold every slot of the tee time invitees join, or the game has no
 * tee time. A full game takes no more yeses.
 */
internal val Game.isFull: Boolean
    // Every game has exactly one tee time for now, and it is the one an invitee joins.
    get() = teeTimes.firstOrNull().let { it == null || it.openSlots == 0 }

/** How full this game is: see [GameFillStatus]. */
internal val Game.fillStatus: GameFillStatus
    get() {
        if (isFull) return GameFillStatus.FULL
        val openSlots = teeTimes.first().openSlots
        val unanswered = invitees.count { it.status == InviteStatus.PENDING }
        return if (unanswered < openSlots) GameFillStatus.OPEN else GameFillStatus.PENDING
    }
