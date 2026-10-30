package net.bradball.teetimecaddie.core.models

/**
 * A person the organizer invited to a game.
 *
 * Whether the invitee already has an account is settled when the game is published, by matching
 * [phone] against players' numbers. The organizer never sees or chooses that distinction.
 *
 * @property name The name the organizer invited them by, e.g. from their contact card.
 * @property phone Digits only, the number they were invited by. A later sign-up with this number
 *   finds the invitation.
 * @property playerId The matching player's id if the number belonged to an account at publish
 *   time, otherwise null — an invitee the organizer manages.
 * @property status Their response so far.
 */
data class GameInvitee(
    val name: String,
    val phone: String,
    val playerId: String?,
    val status: InviteStatus
) {
    /** The single uppercase letter shown in the invitee's avatar. */
    val initial: String
        get() = name.trim().take(1).uppercase()

    /** The color of the invitee's avatar, the same one they get on every screen. */
    val avatarTone: AvatarTone
        get() = AvatarTone.forName(name)
}

val previewGameInvitee = GameInvitee(
    name = "Matt Chen",
    phone = "4155550188",
    playerId = null,
    status = InviteStatus.PENDING
)

val previewGameInviteeList = listOf(
    previewGameInvitee,
    previewGameInvitee.copy(name = "Jeff Morrison", phone = "4155550142", playerId = "previewJeff"),
    previewGameInvitee.copy(name = "Dana Park", phone = "4155550117")
)
