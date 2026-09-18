package net.bradball.teetimecaddie.core.models

import net.bradball.teetimecaddie.core.extensions.formattedPhoneNumber

/**
 * A person with a TeeTimeCaddie account.
 *
 * @property id The Firebase Auth user id. Also the id of the player's document in storage.
 * @property phone Digits only, e.g. `"5025551234"` — never a formatted string. Invitations are
 *   matched on this value, so a number typed at sign-up and the same number read from a device's
 *   contacts have to compare equal. Use [formattedPhone] to display it.
 * @property photoUrl A download URL for the player's avatar, or null if they never added one.
 */
data class Player(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val photoUrl: String? = null
) {
    /** Just the first name — "Brad" from "Brad Ball". Used by the welcome confirmations. */
    val firstName: String
        get() = name.trim().substringBefore(' ')

    /** The single uppercase letter shown in place of a photo when [photoUrl] is null. */
    val initial: String
        get() = name.trim().take(1).uppercase()

    /** [phone] formatted for display, e.g. `"(502) 555-1234"`. */
    val formattedPhone: String
        get() = phone.formattedPhoneNumber
}

val previewPlayer = Player(
    id = "previewPlayer",
    name = "Brad Ball",
    email = "brad@teetimecaddie.app",
    phone = "5025551234",
    photoUrl = null
)

val previewPlayerList = listOf(
    previewPlayer,
    previewPlayer.copy(
        id = "previewPlayer2",
        name = "Dana Park",
        email = "dana@teetimecaddie.app",
        phone = "5025552345"
    ),
    previewPlayer.copy(
        id = "previewPlayer3",
        name = "Sam Pruitt",
        email = "sam@teetimecaddie.app",
        phone = "5025553456"
    ),
    previewPlayer.copy(
        id = "previewPlayer4",
        name = "Jill Marks",
        email = "jill@teetimecaddie.app",
        phone = "5025554567"
    )
)
