package net.bradball.teetimecaddie.features.games

import net.bradball.teetimecaddie.core.models.InviteList
import net.bradball.teetimecaddie.core.models.ManualInviteeCheck
import net.bradball.teetimecaddie.core.models.NewInvitee

/** The outcome of choosing someone entered by name and phone number. */
sealed interface ManualInviteeResult {

    /**
     * The person was chosen.
     *
     * @property person The person as chosen, with their name tidied.
     * @property list The invite list with them at the end of its chosen people.
     */
    data class Chosen(val person: NewInvitee, val list: InviteList) : ManualInviteeResult

    /** Nobody was chosen, because of what [check] reports. */
    data class Rejected(val check: ManualInviteeCheck) : ManualInviteeResult
}
