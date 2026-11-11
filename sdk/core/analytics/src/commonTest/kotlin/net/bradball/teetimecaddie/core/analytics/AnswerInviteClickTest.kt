package net.bradball.teetimecaddie.core.analytics

import kotlin.test.Test
import kotlin.test.assertEquals

class AnswerInviteClickTest {
    @Test
    fun isNamedAnswerInviteClickAndIsAClick() {
        val event = AnalyticsEvent.AnswerInviteClick(
            AnalyticsEvent.AnswerInviteClick.Response.YES,
            AnalyticsEvent.AnswerInviteClick.Source.GAMES_LIST
        )

        assertEquals("answer_invite_click", event.name)
        assertEquals(EventType.CLICK, event.type)
    }

    @Test
    fun carriesTheResponseAndSourceAsTheirSerialNames() {
        val yesFromList = AnalyticsEvent.AnswerInviteClick(
            AnalyticsEvent.AnswerInviteClick.Response.YES,
            AnalyticsEvent.AnswerInviteClick.Source.GAMES_LIST
        )
        val noFromDetails = AnalyticsEvent.AnswerInviteClick(
            AnalyticsEvent.AnswerInviteClick.Response.NO,
            AnalyticsEvent.AnswerInviteClick.Source.GAME_DETAILS
        )

        assertEquals(mapOf("response" to "yes", "source" to "games_list"), yesFromList.parameters())
        assertEquals(mapOf("response" to "no", "source" to "game_details"), noFromDetails.parameters())
    }

    @Test
    fun anAcceptedFlagIsYesAndAnythingElseIsNo() {
        val source = AnalyticsEvent.AnswerInviteClick.Source.GAME_DETAILS

        assertEquals(
            AnalyticsEvent.AnswerInviteClick(AnalyticsEvent.AnswerInviteClick.Response.YES, source),
            AnalyticsEvent.AnswerInviteClick(accepted = true, source = source)
        )
        assertEquals(
            AnalyticsEvent.AnswerInviteClick(AnalyticsEvent.AnswerInviteClick.Response.NO, source),
            AnalyticsEvent.AnswerInviteClick(accepted = false, source = source)
        )
    }

    private fun AnalyticsEvent.parameters() = asMap.filterKeys { it != "type" && it != "name" }
}
