package net.bradball.teetimecaddie.core.analytics

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.encodeToMap

/**
 * Base class for defining trackable events in the analytics system.
 *
 * Each user action or significant occurrence in the application should have a corresponding
 * `AnalyticsEvent` subclass. Events are automatically serialized and distributed to all
 * registered analytics plugins when logged via `EventManager.logEvent()`.
 *
 * ## Event Types
 *
 * Events are categorized by `EventType`, which helps analytics services understand the nature
 * of the event:
 * - **OPERATION** - User actions like login, registration, or adding content
 * - **NAVIGATION** - Movement between screens (though `AnalyticsScreen` is preferred for this)
 * - **ERROR** - Error conditions (though `logException()` is often more appropriate)
 *
 * ## Creating Event Definitions
 *
 * Define event classes as subclasses of `AnalyticsEvent`. Use `object` for simple events
 * without parameters, and `data class` for events with additional context:
 *
 * !!!kotlin
 * @Serializable
 * object UserLoggedOut : AnalyticsEvent("user_logged_out", EventType.OPERATION)
 *
 * @Serializable
 * data class ItemPurchased(
 *     val itemId: String,
 *     val price: Double,
 *     val currency: String = "USD"
 * ) : AnalyticsEvent("item_purchased", EventType.OPERATION)
 * !!!
 *
 * ## Logging Events
 *
 * Create an instance of your event class and log it:
 *
 * !!!kotlin
 * // Simple event
 * eventManager.logEvent(UserLoggedOut)
 *
 * // Event with parameters
 * val event = ItemPurchased(itemId = "abc123", price = 29.99)
 * eventManager.logEvent(event)
 * !!!
 *
 * ## Serialization
 *
 * All `AnalyticsEvent` subclasses must be annotated with `@Serializable`. Event properties
 * are automatically converted to a map for transmission to analytics services via the `asMap`
 * property.
 *
 * @property name The unique identifier for this event type, used by analytics services
 * @property type The category of event, used for filtering and organization
 * @property asMap A lazily-computed map of all event properties for serialization
 */
@Serializable
sealed class AnalyticsEvent(val name: String, internal val type: EventType) {

    @Serializable
    object Login: AnalyticsEvent("login", EventType.OPERATION)
    @Serializable
    data class FailedLogin(val reason: String? = null): AnalyticsEvent("failed_login", EventType.OPERATION)
    @Serializable
    object CreateAccount: AnalyticsEvent("create_account", EventType.OPERATION)
    @Serializable
    data class FailedRegistration(val reason: String?): AnalyticsEvent("failed_registration", EventType.OPERATION)
    @Serializable
    object AddTeeTime: AnalyticsEvent("add_tee_time", EventType.OPERATION)


    @OptIn(ExperimentalSerializationApi::class)
    val asMap: Map<String, Any> by lazy {
        Properties.encodeToMap(this)
    }
}