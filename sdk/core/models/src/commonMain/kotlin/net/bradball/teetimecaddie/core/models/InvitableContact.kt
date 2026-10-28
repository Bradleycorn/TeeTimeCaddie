package net.bradball.teetimecaddie.core.models

/**
 * One way to invite someone from the device's contacts: a contact, and one of their numbers.
 *
 * A contact with several numbers is one of these per number, so the organizer chooses which number
 * the invitation goes to.
 *
 * @property id Unique among the contacts read together, and the same each time the same contacts
 *   are read. Use it to track which rows are checked.
 * @property name The contact's name, without spaces around it.
 * @property phone The number as it is written on the contact, such as `"+44 20 7946 0958"`. It is
 *   normalized when the game is published.
 * @property phoneLabel The number's type as the device names it, such as "mobile", already in the
 *   device's language. Null when the device gives none.
 */
data class InvitableContact(
    val id: String,
    val name: String,
    val phone: String,
    val phoneLabel: String?
) {
    /** The single uppercase letter shown in the contact's avatar. */
    val initial: String
        get() = avatarInitial(name)

    /** The color of the contact's avatar, the same one they keep once invited. */
    val avatarTone: AvatarTone
        get() = AvatarTone.forName(name)

    /**
     * [phone] for display: formatted like `"(415) 555-0188"` when it is a 10-digit number, and as
     * written otherwise.
     */
    val formattedPhone: String
        get() = displayPhone(phone)

    /** This contact as someone to invite, by this number. */
    fun toNewInvitee(): NewInvitee = NewInvitee(name = name, phone = phone)
}

val previewInvitableContact = InvitableContact(
    id = "preview-matt|4155550188",
    name = "Matt Chen",
    phone = "(415) 555-0188",
    phoneLabel = "mobile"
)

val previewInvitableContactList = listOf(
    InvitableContact(id = "preview-dana|4155550117", name = "Dana Park", phone = "415.555.0117", phoneLabel = "mobile"),
    InvitableContact(id = "preview-jeff|4155550142", name = "Jeff Morrison", phone = "(415) 555-0142", phoneLabel = "mobile"),
    InvitableContact(id = "preview-jeff|5025550199", name = "Jeff Morrison", phone = "502-555-0199", phoneLabel = "work"),
    previewInvitableContact,
    InvitableContact(id = "preview-priya|442079460958", name = "Priya Shah", phone = "+44 20 7946 0958", phoneLabel = null)
)
