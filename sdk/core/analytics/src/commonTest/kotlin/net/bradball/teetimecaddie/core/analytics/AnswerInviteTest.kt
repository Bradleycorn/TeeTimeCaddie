package net.bradball.teetimecaddie.core.analytics

import kotlin.test.Test
import kotlin.test.assertEquals

class AnswerInviteTest {
    @Test
    fun isNamedAnswerInviteAndIsAnOperation() {
        val event = AnalyticsEvent.AnswerInvite(accepted = true, AnalyticsEvent.AnswerInvite.Source.PLAYER)

        assertEquals("answer_invite", event.name)
        assertEquals(EventType.OPERATION, event.type)
    }

    @Test
    fun carriesTheAnswerAndWhoGaveIt() {
        val playerYes = AnalyticsEvent.AnswerInvite(accepted = true, AnalyticsEvent.AnswerInvite.Source.PLAYER)
        val organizerNo = AnalyticsEvent.AnswerInvite(accepted = false, AnalyticsEvent.AnswerInvite.Source.ORGANIZER)

        assertEquals(mapOf("accepted" to true, "source" to "player"), playerYes.parameters())
        assertEquals(mapOf("accepted" to false, "source" to "organizer"), organizerNo.parameters())
    }

    private fun AnalyticsEvent.parameters() = asMap.filterKeys { it != "type" && it != "name" }
}
