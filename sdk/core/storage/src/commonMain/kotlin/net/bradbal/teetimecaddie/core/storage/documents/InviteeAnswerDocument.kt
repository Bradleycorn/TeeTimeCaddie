package net.bradbal.teetimecaddie.core.storage.documents

/**
 * What answering an invitation writes, in one transaction.
 *
 * @property invitee The invitee's document as answered.
 * @property seat For a yes, the game's roster with the invitee seated. Null for a no, which leaves
 *   the game alone.
 */
data class InviteeAnswerDocument(
    val invitee: GameInviteeDocument,
    val seat: GameRosterDocument?
)
