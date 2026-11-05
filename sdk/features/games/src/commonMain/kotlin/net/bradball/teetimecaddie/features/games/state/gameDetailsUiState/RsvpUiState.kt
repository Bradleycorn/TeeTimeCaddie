package net.bradball.teetimecaddie.features.games.state.gameDetailsUiState

import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.features.games.GMR

/** Where the signed-in player stands on their invitation, which decides the RSVP card. */
sealed interface RsvpUiState {
    /**
     * Not answered yet: the card asks, with Yes and No.
     *
     * @property title Who's asking, e.g. "Brad invited you to play". Null if the organizer isn't
     *   known.
     * @property prompt "Can you make it?", or, once confirmed players fill every slot, "All 4
     *   spots are taken."
     * @property canAccept False once every slot is taken. Yes is then disabled; No still works.
     */
    data class Pending(
        val prompt: LocalizedText,
        val canAccept: Boolean,
        val title: LocalizedText? = null
    ) : RsvpUiState

    /** Said yes, and holds a slot. The card collapses to "You're in". */
    data object Accepted : RsvpUiState {
        val message = LocalizedText(GMR.strings.game_details_rsvp_accepted)
    }

    /** Said no. The card collapses to "You declined". */
    data object Declined : RsvpUiState {
        val message = LocalizedText(GMR.strings.game_details_rsvp_declined)
    }
}

/** The RSVP card for whoever was invited by [phone], or null if nobody was. */
internal fun Game.rsvpFor(phone: String): RsvpUiState? {
    val invitee = inviteeFor(phone) ?: return null
    return when (invitee.status) {
        InviteStatus.ACCEPTED -> RsvpUiState.Accepted
        InviteStatus.DECLINED -> RsvpUiState.Declined
        InviteStatus.PENDING -> {
            // Every game has exactly one tee time for now, and it is the one an invitee joins.
            val teeTime = teeTimes.firstOrNull()
            val title = organizerFirstName()?.let { LocalizedText(GMR.strings.game_details_rsvp_title, listOf(it)) }
            if (teeTime == null || teeTime.openSlots == 0) {
                RsvpUiState.Pending(
                    prompt = LocalizedText(GMR.strings.game_details_rsvp_full, listOf(teeTime?.slots?.toString().orEmpty())),
                    canAccept = false,
                    title = title
                )
            } else {
                RsvpUiState.Pending(
                    prompt = LocalizedText(GMR.strings.game_details_rsvp_prompt),
                    canAccept = true,
                    title = title
                )
            }
        }
    }
}
