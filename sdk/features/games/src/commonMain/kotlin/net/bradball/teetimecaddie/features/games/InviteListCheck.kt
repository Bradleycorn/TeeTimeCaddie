package net.bradball.teetimecaddie.features.games

import net.bradball.teetimecaddie.core.extensions.PHONE_NUMBER_LENGTH
import net.bradball.teetimecaddie.core.extensions.toPhoneDigits
import net.bradball.teetimecaddie.core.models.InviteList
import net.bradball.teetimecaddie.core.models.InviteePhoneError
import net.bradball.teetimecaddie.core.models.ManualInviteeCheck

/**
 * What's wrong, if anything, with a name and phone number entered to be chosen for this list.
 *
 * The name must not be blank, and the number must have exactly 10 digits and not be in the list.
 * [ContactsRepository.chooseEnteredPerson] runs this check; shared UI state uses it to show field
 * errors before anyone is chosen.
 */
internal fun InviteList.check(name: String, phone: String): ManualInviteeCheck {
    val digits = phone.toPhoneDigits()
    val phoneError = when {
        digits.isEmpty() -> InviteePhoneError.MISSING
        digits.length != PHONE_NUMBER_LENGTH -> InviteePhoneError.INCOMPLETE
        contains(digits) -> InviteePhoneError.DUPLICATE
        else -> null
    }
    return ManualInviteeCheck(isNameBlank = name.isBlank(), phoneError = phoneError)
}
