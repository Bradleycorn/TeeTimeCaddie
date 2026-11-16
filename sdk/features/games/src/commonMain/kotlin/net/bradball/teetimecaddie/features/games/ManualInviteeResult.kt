package net.bradball.teetimecaddie.features.games

import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.ManualInviteeCheck

/** The outcome of selecting someone entered manually by name and number. */
sealed interface ManualInviteeResult {

    /**
     * The person was added to the selection.
     *
     * @property invitee The person as added, with their name tidied.
     * @property selection The selection with them at the end.
     */
    data class Selected(val invitee: NewInvitee, val selection: List<NewInvitee>) : ManualInviteeResult

    /** Nobody was added, because of what [check] reports. */
    data class Rejected(val check: ManualInviteeCheck) : ManualInviteeResult
}
