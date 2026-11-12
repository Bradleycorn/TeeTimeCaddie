package net.bradbal.teetimecaddie.core.storage.documents

import kotlinx.serialization.Serializable

/**
 * Someone invited to a game, stored at `games/{gameId}/invitees/{phone}`: one document per invitee,
 * under the phone number (digits only) they were invited by.
 *
 * Its own document, rather than an entry on the game, so every answer is a write the security rules
 * can tie to exactly one person through the document's path.
 *
 * @property playerId The player who owned the number when the game was published, or null for an
 *   invitee the organizer manages. Becomes the answering player's id once they answer.
 * @property status The name of an `InviteStatus`, e.g. "PENDING".
 * @property order The position the organizer added them in, since documents have no order of
 *   their own.
 */
@Serializable
data class GameInviteeDocument(
    val name: String,
    val playerId: String?,
    val status: String,
    val order: Int
)
