package net.bradball.teetimecaddie.core.models

/**
 * Someone the organizer has picked to invite, before the game is published.
 *
 * @property name The name to show them under.
 * @property phone The number to invite them by, in any format; it is normalized on publish.
 */
data class NewInvitee(
    val name: String,
    val phone: String
)

val previewNewInvitee = NewInvitee(name = "Matt Chen", phone = "(415) 555-0188")
