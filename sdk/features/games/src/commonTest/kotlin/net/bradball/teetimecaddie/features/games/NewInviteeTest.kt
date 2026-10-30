package net.bradball.teetimecaddie.features.games

import net.bradball.teetimecaddie.core.models.NewInvitee
import kotlin.test.Test
import kotlin.test.assertEquals

class NewInviteeTest {

    @Test
    fun theInitialIsTheFirstLetterUppercased() {
        assertEquals("M", NewInvitee(name = " matt Chen", phone = "4155550188").initial)
    }

    @Test
    fun aTenDigitNumberIsFormattedHoweverItWasWritten() {
        for (phone in listOf("4155550188", "+1 415 555 0188", "415.555.0188")) {
            assertEquals("(415) 555-0188", NewInvitee(name = "Matt", phone = phone).formattedPhone)
        }
    }

    @Test
    fun anyOtherNumberIsShownAsGiven() {
        assertEquals("+44 20 7946 0958", NewInvitee(name = "Ian", phone = " +44 20 7946 0958 ").formattedPhone)
    }
}
