package net.bradball.teetimecaddie.core.models

/**
 * What's wrong, if anything, with a name and phone number typed in to invite someone.
 *
 * @property isNameBlank True when the name is empty or only spaces.
 * @property phoneError What's wrong with the number, or null when it can be added.
 */
data class TypedInviteeCheck(
    val isNameBlank: Boolean,
    val phoneError: TypedPhoneError?
) {
    /** True when the person can be added: a name, and a complete number nobody else has. */
    val isValid: Boolean
        get() = !isNameBlank && phoneError == null
}

/** Why a typed phone number can't be added. */
enum class TypedPhoneError {
    /** Nothing was typed. */
    MISSING,

    /** Fewer or more than 10 digits. */
    INCOMPLETE,

    /** The number is already invited, already selected, or the organizer's own. */
    DUPLICATE,
}

/** A name and number that can be added. */
val previewTypedInviteeCheck = TypedInviteeCheck(isNameBlank = false, phoneError = null)
