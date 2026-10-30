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
) {
    /** The single uppercase letter shown in the invitee's avatar. */
    val initial: String
        get() = avatarInitial(name)

    /** The color of the invitee's avatar, the same one they get on every screen. */
    val avatarTone: AvatarTone
        get() = AvatarTone.forName(name)

    /**
     * [phone] for display: formatted like `"(415) 555-0188"` when it is a 10-digit number, and as
     * it was given otherwise. Formatting keeps only 10 digits, so it would garble a longer
     * international number.
     */
    val formattedPhone: String
        get() = displayPhone(phone)
}

val previewNewInvitee = NewInvitee(name = "Matt Chen", phone = "(415) 555-0188")

val previewNewInviteeList = listOf(
    previewNewInvitee,
    NewInvitee(name = "Jeff Morrison", phone = "+1 415 555 0142"),
    NewInvitee(name = "Dana Park", phone = "415.555.0117")
)
