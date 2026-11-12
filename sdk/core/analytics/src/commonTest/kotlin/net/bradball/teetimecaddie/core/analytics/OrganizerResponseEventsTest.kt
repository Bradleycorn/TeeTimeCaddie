package net.bradball.teetimecaddie.core.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrganizerResponseEventsTest {
    @Test
    fun inviteeRowClickIsAClickWithNoParameters() {
        val event = AnalyticsEvent.InviteeRowClick

        assertEquals("invitee_row_click", event.name)
        assertEquals(EventType.CLICK, event.type)
        assertTrue(event.parameters().isEmpty())
    }

    @Test
    fun organizerResponseClickIsAClickCarryingTheOptionPicked() {
        val yes = AnalyticsEvent.OrganizerResponseClick(AnalyticsEvent.OrganizerResponseClick.Response.YES)
        val no = AnalyticsEvent.OrganizerResponseClick(AnalyticsEvent.OrganizerResponseClick.Response.NO)

        assertEquals("organizer_response_click", yes.name)
        assertEquals(EventType.CLICK, yes.type)
        assertEquals(mapOf("response" to "yes"), yes.parameters())
        assertEquals(mapOf("response" to "no"), no.parameters())
    }

    @Test
    fun organizerResponseClickTakesAnAcceptedFlag() {
        assertEquals(
            AnalyticsEvent.OrganizerResponseClick(AnalyticsEvent.OrganizerResponseClick.Response.YES),
            AnalyticsEvent.OrganizerResponseClick(accepted = true)
        )
        assertEquals(
            AnalyticsEvent.OrganizerResponseClick(AnalyticsEvent.OrganizerResponseClick.Response.NO),
            AnalyticsEvent.OrganizerResponseClick(accepted = false)
        )
    }

    @Test
    fun organizerResponseSheetDismissIsAClickWithNoParameters() {
        val event = AnalyticsEvent.OrganizerResponseSheetDismiss

        assertEquals("organizer_response_sheet_dismiss", event.name)
        assertEquals(EventType.CLICK, event.type)
        assertTrue(event.parameters().isEmpty())
    }

    private fun AnalyticsEvent.parameters() = asMap.filterKeys { it != "type" && it != "name" }
}
