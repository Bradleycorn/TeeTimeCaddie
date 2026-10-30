package net.bradball.teetimecaddie.core.models

import net.bradball.teetimecaddie.core.extensions.PHONE_NUMBER_LENGTH
import net.bradball.teetimecaddie.core.extensions.formattedPhoneNumber
import net.bradball.teetimecaddie.core.extensions.toPhoneDigits

// How a person being invited is shown, shared by the models that describe one, so a contact in the
// picker and the invitee it becomes look the same.

/** The single uppercase letter shown in the avatar of someone named [name]. */
internal fun avatarInitial(name: String): String = name.trim().take(1).uppercase()

/**
 * [phone] for display: formatted like `"(415) 555-0188"` when it is a 10-digit number, and as it
 * was given otherwise. Formatting keeps only 10 digits, so it would garble a longer international
 * number.
 */
internal fun displayPhone(phone: String): String =
    if (phone.toPhoneDigits().length == PHONE_NUMBER_LENGTH) {
        phone.formattedPhoneNumber
    } else {
        phone.trim()
    }
