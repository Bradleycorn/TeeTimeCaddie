package net.bradball.teetimecaddie.core.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddPlayersEventsTest {
    @Test
    fun theAddPlayersClicksAreClicksWithNoParameters() {
        val expected = mapOf(
            AnalyticsEvent.AddPlayersClick to "add_players_click",
            AnalyticsEvent.ChooseFromContactsClick to "choose_from_contacts_click",
            AnalyticsEvent.AddEnteredPlayerClick to "add_entered_player_click",
            AnalyticsEvent.UnchoosePlayerClick to "unchoose_player_click",
            AnalyticsEvent.AddChosenPlayersClick to "add_chosen_players_click",
            AnalyticsEvent.AddPlayersSheetDismiss to "add_players_sheet_dismiss",
        )

        expected.forEach { (event, name) ->
            assertEquals(name, event.name)
            assertEquals(EventType.CLICK, event.type)
            assertTrue(event.parameters().isEmpty(), "$name has no parameters")
        }
    }

    @Test
    fun invitePlayersIsAnOperationCarryingThePlayerCount() {
        val event = AnalyticsEvent.InvitePlayers(playerCount = 3)

        assertEquals("invite_players", event.name)
        assertEquals(EventType.OPERATION, event.type)
        assertEquals(mapOf<String, Any>("player_count" to 3), event.parameters())
    }

    private fun AnalyticsEvent.parameters() = asMap.filterKeys { it != "type" && it != "name" }
}
