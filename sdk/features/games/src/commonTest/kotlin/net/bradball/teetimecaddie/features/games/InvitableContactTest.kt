package net.bradball.teetimecaddie.features.games

import net.bradball.teetimecaddie.core.models.InvitableContact
import net.bradball.teetimecaddie.core.models.NewInvitee
import kotlin.test.Test
import kotlin.test.assertEquals

class InvitableContactTest {

    private val matt = InvitableContact("c1|4155550188", "Matt Chen", "4155550188", "mobile")

    @Test
    fun aContactLooksTheSameAsTheInviteeItBecomes() {
        val invitee = matt.toNewInvitee()

        assertEquals(NewInvitee(name = "Matt Chen", phone = "4155550188"), invitee)
        assertEquals(invitee.initial, matt.initial)
        assertEquals(invitee.avatarTone, matt.avatarTone)
        assertEquals("(415) 555-0188", matt.formattedPhone)
    }

    @Test
    fun anInternationalNumberIsShownAsWritten() {
        val priya = InvitableContact("c2|442079460958", "Priya Shah", "+44 20 7946 0958", "mobile")

        assertEquals("+44 20 7946 0958", priya.formattedPhone)
        assertEquals("+44 20 7946 0958", priya.toNewInvitee().formattedPhone)
    }
}
