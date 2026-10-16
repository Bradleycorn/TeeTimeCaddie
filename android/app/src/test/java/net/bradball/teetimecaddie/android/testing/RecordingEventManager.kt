package net.bradball.teetimecaddie.android.testing

import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.EventPlugin

/**
 * An [EventManager] with no loggers, plus the events it was asked to log, for asserting on
 * analytics in ViewModel tests.
 */
class RecordingEventManager {
    val events = mutableListOf<AnalyticsEvent>()

    val eventManager: EventManager = EventManager.getInstance(enableLogging = false).apply {
        registerPlugin(object : EventPlugin {
            override fun logEvent(event: AnalyticsEvent): Boolean = events.add(event)
            override fun setUserId(userId: String) = Unit
        })
    }
}
