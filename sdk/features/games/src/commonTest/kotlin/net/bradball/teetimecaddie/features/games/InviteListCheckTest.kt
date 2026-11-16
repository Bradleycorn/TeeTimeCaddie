package net.bradball.teetimecaddie.features.games

import net.bradball.teetimecaddie.core.models.InviteList
import net.bradball.teetimecaddie.core.models.InviteePhoneError
import net.bradball.teetimecaddie.core.models.ManualInviteeCheck
import net.bradball.teetimecaddie.core.models.NewInvitee
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InviteListCheckTest {

    private val list = InviteList(
        organizerPhone = "5025551234",
        invited = listOf(NewInvitee("Matt Chen", "(415) 555-0188")),
        chosen = listOf(NewInvitee("Dana Park", "4155550117"))
    )

    @Test
    fun aNameAndACompleteNewNumberAreValid() {
        assertEquals(ManualInviteeCheck(isNameBlank = false, phoneError = null), list.check("Sam Tanaka", "(415) 555-0123"))
    }

    @Test
    fun aBlankOrSpacesOnlyNameIsBlank() {
        assertTrue(list.check("", "4155550123").isNameBlank)
        assertTrue(list.check("   ", "4155550123").isNameBlank)
    }

    @Test
    fun aMissingNumberIsMissing() {
        assertEquals(InviteePhoneError.MISSING, list.check("Sam", "").phoneError)
    }

    @Test
    fun aNumberShortOfTenDigitsIsIncompleteEvenIfItStartsLikeADuplicate() {
        assertEquals(InviteePhoneError.INCOMPLETE, list.check("Sam", "(415) 555-018").phoneError)
    }

    @Test
    fun aNumberInvitedChosenOrTheOrganizersIsADuplicateWhateverTheName() {
        assertEquals(InviteePhoneError.DUPLICATE, list.check("Matthew", "4155550188").phoneError)
        assertEquals(InviteePhoneError.DUPLICATE, list.check("Dana P", "415-555-0117").phoneError)
        assertEquals(InviteePhoneError.DUPLICATE, list.check("Me", "(502) 555-1234").phoneError)
    }

    @Test
    fun bothFieldsAreCheckedTogether() {
        assertEquals(ManualInviteeCheck(isNameBlank = true, phoneError = InviteePhoneError.MISSING), list.check(" ", ""))
    }
}
