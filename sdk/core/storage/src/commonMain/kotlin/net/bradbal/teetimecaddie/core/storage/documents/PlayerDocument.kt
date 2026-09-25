package net.bradbal.teetimecaddie.core.storage.documents

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Document model for storing a player in Firestore.
 *
 * The document's id is the player's Firebase Auth user id, so a player document is always
 * addressable from a session without a query.
 *
 * @property phone Digits only. The security rules enforce this server-side, because invitation
 *   matching compares these values directly and a formatted number would never match.
 */
@Serializable
data class PlayerDocument(
    val name: String,
    val email: String,
    val phone: String,
    val photoUrl: String? = null
) {

    @Transient
    var id: String? = null
}
