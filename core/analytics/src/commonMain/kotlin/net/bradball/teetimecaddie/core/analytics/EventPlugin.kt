package net.bradball.teetimecaddie.core.analytics

import kotlin.reflect.KClass

/**
 * Defines the interface that must be implemented for each third party service that wishes to track events and/or analytics.
 *
 * When a specific service (Google Analytics, Kochava, etc) is used to track analytics or event data, an implementation
 * of this interface should be created for that service. In order to get notified of events, an instance of the
 * implementation should be registered with the [EventManager] as early as possible after app launch. No events
 * will be passed to your implementation until it is registered, and there is no mechanism to be notified of
 * "previous" events at registration time.
 */
interface EventPlugin {

    /**
     * Called by the [EventManager] when an event is logged.
     *
     * This is this is the primary interface through which your plugin
     * will be informed that a new event has been fired and needs to be logged.
     *
     * Parse the provided event data, and determine if the service cares about the event
     * or should ignore it. Return a boolean indicating if the event was passed on to the service
     * or not.
     *
     * @param event - The Event that should be logged.
     * Events include a Type that can be used as a filter or category.
     * Events also include payload containing additional data that can be logged with the event
     *
     * @return Boolean - true if your plugin logged the event, false if it did not.
     */
    fun logEvent(event: AnalyticsEvent): Boolean

    /**
     * Called by the [EventManager] when user attributes need to be set
     *
     * @param attributes a class that contains all the attributes that can be set by the provider
     *
     */
    //fun updateUserAttributes(attributes: UserAttributes)

    /**
     * Called by the event manager when the user logs in, or when a logged in user launches the app.
     *
     * Some services may want or need a user identifer in order to properly track events
     * for specific users. This method can be implemented to get the user id when it becomes
     * available and send it to the tracking service.
     *
     * @param userId A string the uniquely identifes the user (in our case, cam ID)
     */
    fun setUserId(userId: String)

    /**
     * Called by the [EventManager] when a new screen is viewed by the user.
     * The screen could be a complete screen (Today's Races, Account History, etc),
     * or a screen in a ViewPager (RaceFragment, Probables, etc), or a Full Screen Dialog (Race Picker, etc).
     *
     * If an analytics service wants to log screen views, it can implement this method to do so.
     * A default (empty) implementation is provided in the interface, so that Plugins for services that do
     * not care about screen views can ignore this method entirely.
     *
     * @param screenName - A display name for the screen being logged (eg "Todays Races")
     * @param screenClass - The Kotlin KClass for the screen being logged (eg TodaysRacesFragment::class)
     *
     * @return Boolean - true if the screen view was logged, false if it was not. Default implementation returns false.
     */
    fun logScreenView(screenName: String, extras: Map<String, String> = emptyMap()): Boolean = false
}
