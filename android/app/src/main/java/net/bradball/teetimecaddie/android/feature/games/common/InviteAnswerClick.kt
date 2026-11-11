package net.bradball.teetimecaddie.android.feature.games.common

import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent

/** The response a tap on Yes ([this] is true) or No is logged with. */
fun Boolean.toResponse(): AnalyticsEvent.AnswerInviteClick.Response =
    if (this) AnalyticsEvent.AnswerInviteClick.Response.YES else AnalyticsEvent.AnswerInviteClick.Response.NO
