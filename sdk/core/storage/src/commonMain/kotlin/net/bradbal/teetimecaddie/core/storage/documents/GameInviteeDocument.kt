package net.bradbal.teetimecaddie.core.storage.documents

import kotlinx.serialization.Serializable

/**
 * Someone invited to a game, stored in the game's `invitees` map under the phone number they were
 * invited by.
 *
 * The phone is the map key rather than a field, so the security rules can pick out one invitee's
 * entry and check that an answer changed only that one.
 *
 * @property playerId The player who owned the number when the game was published, or null for an
 *   invitee the organizer manages. Becomes the answering player's id once they answer.
 * @property status The name of an `InviteStatus`, e.g. "PENDING".
 * @property order The position the organizer added them in. A map has no order of its own.
 */
@Serializable
data class GameInviteeDocument(
    val name: String,
    val playerId: String?,
    val status: String,
    val order: Int
)
