package net.bradball.teetimecaddie.features.games.state.gameDetailsUiState

import net.bradball.teetimecaddie.core.models.AvatarTone
import net.bradball.teetimecaddie.core.models.LocalizedText

/**
 * One row of a tee time's roster.
 *
 * @property initial The letter in the person's avatar.
 * @property name The person's full name, as shown for everyone but the signed-in player.
 * @property tone The avatar's color: the organizer's gold, or the person's own color from their
 *   name, which matches the one they had on the New game screen.
 * @property status Where they stand, which decides the row's label.
 * @property viewerName Set only on the signed-in player's own row in the invitee view, which
 *   shows this in place of [name]: their first name, marked "Dana (you)". The organizer's own row
 *   keeps their name and the "Organizer" label.
 */
data class TeeTimePlayerRowUiState(
    val initial: String,
    val name: String,
    val tone: AvatarTone,
    val status: TeeTimePlayerStatus,
    val viewerName: LocalizedText? = null
)

/** Where a person on a tee time's roster stands, which decides how their row is labelled. */
enum class TeeTimePlayerStatus {
    /** Organized the game, and holds a slot. Labelled "Organizer". */
    ORGANIZER,

    /** Holds a slot: said yes to their invitation, or was added without one. Labelled "Confirmed". */
    CONFIRMED,

    /** Invited, and hasn't answered. Labelled "Invited". */
    INVITED
}
