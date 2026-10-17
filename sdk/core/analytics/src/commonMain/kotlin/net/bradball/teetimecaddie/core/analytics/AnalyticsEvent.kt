package net.bradball.teetimecaddie.core.analytics

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
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
    /**
     * The Firebase Auth account was created, but the person has not completed their profile yet.
     * Paired with [CreateAccount] this measures drop-off on the second step of sign-up, which the
     * two-phase flow makes a real risk.
     */
    @Serializable
    object CreateAccountStarted: AnalyticsEvent("create_account_started", EventType.OPERATION)
    @Serializable
    data class AbandonedRegistration(val reason: String? = null): AnalyticsEvent("abandoned_registration", EventType.OPERATION)
    @Serializable
    object SignOut: AnalyticsEvent("sign_out", EventType.OPERATION)
    /** An organizer published a new Game. [slots] is how many players its tee time holds. */
    @Serializable
    data class PublishGame(val slots: Int): AnalyticsEvent("publish_game", EventType.OPERATION)
    /** A Game was tapped on the Games tab, opening its details. */
    @Serializable
    object GameListItemClick: AnalyticsEvent("game_list_item_click", EventType.CLICK)
    /** The add control on the Games tab was tapped: the FAB or + button, or the empty state's "Create a game". */
    @Serializable
    data class AddGameClick(val source: Source): AnalyticsEvent("add_game_click", EventType.CLICK) {
        @Serializable
        enum class Source {
            @SerialName("add_button") ADD_BUTTON,
            @SerialName("empty_list") EMPTY_LIST
        }
    }
    /** One of the New game screen's player-slot buttons was tapped. [playerCount] is the number tapped. */
    @Serializable
    data class PlayerSlotsClick(
        @SerialName("player_count") val playerCount: Int
    ): AnalyticsEvent("player_slots_click", EventType.CLICK)
    /** The date picker was switched between its calendar and typed-entry modes. */
    @Serializable
    object DateInputToggle: AnalyticsEvent("date_input_toggle", EventType.CLICK)
    /** The time picker was switched between its dial and typed-entry modes. */
    @Serializable
    object TimeInputToggle: AnalyticsEvent("time_input_toggle", EventType.CLICK)
    /** The New game screen's back / Cancel button was tapped, leaving without publishing. */
    @Serializable
    object NewGameCancel: AnalyticsEvent("new_game_cancel", EventType.CLICK)
    /** A button on the "Publish with no invitees?" alert was tapped. */
    @Serializable
    data class NoInvites(val response: Response): AnalyticsEvent("no_invites", EventType.CLICK) {
        /** Which button closed the alert. */
        @Serializable
        enum class Response {
            @SerialName("edit") KEEP_EDITING,
            @SerialName("publish") PUBLISH_ANYWAY,
        }
    }
    /** A tab in the bottom tab bar was tapped. */
    @Serializable
    data class TabBarClick(val tab: Tab): AnalyticsEvent("tab_bar_click", EventType.CLICK) {
        /** The tabs in the bottom tab bar, as analytics names them. */
        @Serializable
        enum class Tab {
            @SerialName("games") GAMES,
            @SerialName("profile") PROFILE,
        }
    }


    @OptIn(ExperimentalSerializationApi::class)
    val asMap: Map<String, Any> by lazy {
        Properties.encodeToMap(this)
    }
}
