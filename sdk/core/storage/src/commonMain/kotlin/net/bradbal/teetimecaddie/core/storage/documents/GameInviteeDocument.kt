package net.bradbal.teetimecaddie.core.storage.documents

import kotlinx.serialization.Serializable

/**
 * Someone invited to a game, stored in the game's `invitees` list.
 *
 * @property phone Digits only, as on `players` documents, so a sign-up with this number can be
 *   matched to the invitation by a straight comparison.
 * @property playerId The player who owned [phone] when the game was published, or null for an
 *   invitee the organizer manages.
 * @property status The name of an `InviteStatus`, e.g. "PENDING".
 */
@Serializable
data class GameInviteeDocument(
    val name: String,
    val phone: String,
    val playerId: String?,
    val status: String
)
