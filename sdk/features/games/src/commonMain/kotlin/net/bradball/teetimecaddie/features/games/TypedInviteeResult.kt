package net.bradball.teetimecaddie.features.games

import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.TypedInviteeCheck

/** The outcome of selecting someone by a typed name and number. */
sealed interface TypedInviteeResult {

    /**
     * The person was added to the selection.
     *
     * @property invitee The person as added, with their name tidied.
     * @property selection The selection with them at the end.
     */
    data class Selected(val invitee: NewInvitee, val selection: List<NewInvitee>) : TypedInviteeResult

    /** Nobody was added, because of what [check] reports. */
    data class Rejected(val check: TypedInviteeCheck) : TypedInviteeResult
}
