package net.bradball.teetimecaddie.features.games

import net.bradball.teetimecaddie.core.models.InviteList
import net.bradball.teetimecaddie.core.models.NewInvitee
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InviteListTest {

    private val matt = NewInvitee("Matt Chen", "(415) 555-0188")
    private val dana = NewInvitee("Dana Park", "4155550117")
    private val jeff = NewInvitee("Jeff Morrison", "4155550142")
    private val list = InviteList(organizerPhone = "5025551234", invited = listOf(matt), chosen = listOf(dana))

    @Test
    fun containsMatchesInvitedChosenAndTheOrganizerHoweverWritten() {
        assertTrue(list.contains("+1 415-555-0188"))
        assertTrue(list.contains("415.555.0117"))
        assertTrue(list.contains("(502) 555-1234"))
        assertFalse(list.contains("4155550142"))
    }

    @Test
    fun chooseAddsToTheEndOfChosen() {
        assertEquals(listOf(dana, jeff), list.choose(listOf(jeff)).chosen)
    }

    @Test
    fun chooseSkipsNumbersAlreadyInvitedChosenOrTheOrganizers() {
        val again = listOf(
            NewInvitee("Matthew Chen", "4155550188"),
            NewInvitee("Dana P", "415-555-0117"),
            NewInvitee("Me", "5025551234"),
        )

        assertEquals(list, list.choose(again))
    }

    @Test
    fun chooseTakesARepeatedNumberOnceUnderTheFirstName() {
        val alex = NewInvitee("Alex Kim", "4155550150")

        assertEquals(listOf(dana, alex), list.choose(listOf(alex, NewInvitee("Alexander Kim", "4155550150"))).chosen)
    }

    @Test
    fun unchooseRemovesThePersonByNumber() {
        assertEquals(emptyList(), list.unchoose(dana.copy(phone = "415-555-0117")).chosen)
        assertEquals(listOf(matt), list.unchoose(dana).invited)
    }

    @Test
    fun inviteChosenMovesChosenToTheEndOfInvitedInOrder() {
        val invited = list.choose(listOf(jeff)).inviteChosen()

        assertEquals(listOf(matt, dana, jeff), invited.invited)
        assertEquals(emptyList(), invited.chosen)
    }
}
