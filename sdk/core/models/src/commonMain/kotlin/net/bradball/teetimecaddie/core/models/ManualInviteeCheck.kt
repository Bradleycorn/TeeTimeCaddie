package net.bradball.teetimecaddie.core.models

/**
 * What's wrong, if anything, with a name and phone number entered manually to invite someone.
 *
 * @property isNameBlank True when the name is empty or only spaces.
 * @property phoneError What's wrong with the number, or null when it can be added.
 */
data class ManualInviteeCheck(
    val isNameBlank: Boolean,
    val phoneError: InviteePhoneError?
) {
    /** True when the person can be added: a name, and a complete number nobody else has. */
    val isValid: Boolean
        get() = !isNameBlank && phoneError == null
}

/** Why a manually entered phone number can't be added. */
enum class InviteePhoneError {
    /** No number was entered. */
    MISSING,

    /** Fewer or more than 10 digits. */
    INCOMPLETE,

    /** The number is already invited, already selected, or the organizer's own. */
    DUPLICATE,
}

/** A name and number that can be added. */
val previewManualInviteeCheck = ManualInviteeCheck(isNameBlank = false, phoneError = null)
