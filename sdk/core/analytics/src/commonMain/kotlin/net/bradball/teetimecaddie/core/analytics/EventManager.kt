package net.bradball.teetimecaddie.core.analytics

import co.touchlab.kermit.Logger
import net.bradball.teetimecaddie.core.analytics.firebase.FirebaseErrorLogger
import net.bradball.teetimecaddie.core.analytics.firebase.FirebaseTransactionLogger
import net.bradball.teetimecaddie.core.extensions.empty
import kotlin.native.HiddenFromObjC
import kotlin.native.HidesFromObjC
import kotlin.reflect.KClass

/**
 * The main entry point for the application's analytics and error logging system.
 *
 * `EventManager` provides a unified interface for tracking events, screen views, transactions,
 * and errors across multiple analytics services. It uses a plugin-based architecture that allows
 * applications to easily integrate with any number of third-party analytics providers without
 * changing application code.
 *
 * ## Architecture
 *
 * The analytics system is built around three core components:
 *
 * 1. **EventManager** - The central coordinator that receives all analytics calls and distributes
 *    them to registered plugins
 * 2. **EventPlugin** - An interface that analytics service providers implement to receive events,
 *    screen views, and user information
 * 3. **ErrorLogger** and **TransactionLogger** - Specialized loggers for error tracking and
 *    performance monitoring
 *
 * ## Setup
 *
 * To use the analytics system, first create an instance of `EventManager` and register any
 * analytics plugins:
 *
 * ```kotlin
 * val eventManager = EventManager()
 * eventManager.registerPlugin(MyAnalyticsPlugin())
 * eventManager.registerPlugin(AnotherAnalyticsPlugin())
 * ```
 *
 * ## Usage
 *
 * ### Tracking Events
 *
 * Create an `AnalyticsEvent` subclass and log it:
 *
 * ```kotlin
 * val event = MyCustomEvent(parameter1 = "value1")
 * eventManager.logEvent(event)
 * ```
 *
 * ### Tracking Screen Views
 *
 * Create an `AnalyticsScreen` and log it:
 *
 * ```kotlin
 * val screen = MyScreen(userId = "123")
 * eventManager.logScreenView(screen)
 * ```
 *
 * ### Setting User Identity
 *
 * When a user logs in, set their ID to associate all future events with that user:
 *
 * ```kotlin
 * eventManager.setUserId("user123")
 * ```
 *
 * ### Tracking Performance
 *
 * Use transactions to measure performance of key operations:
 *
 * ```kotlin
 * eventManager.startTransaction("load_data")
 * // ... perform operation ...
 * eventManager.stopTransaction("load_data")
 * ```
 *
 * ### Error Logging
 *
 * Log exceptions and messages for debugging:
 *
 * ```kotlin
 * try {
 *     // risky operation
 * } catch (e: Exception) {
 *     eventManager.logException(e, LoggableExceptionTypes.NETWORK_ERROR)
 * }
 * ```
 *
 * ## Plugin System
 *
 * The plugin architecture allows the same analytics code to send data to multiple services
 * simultaneously. Each `EventPlugin` implementation decides which events it's interested in
 * and how to format them for its specific analytics service.
 *
 * If an event or screen view is logged but no registered plugin handles it, an exception
 * is logged to alert developers that the tracking code may be obsolete.
 *
 * @property errorLogger The logger used for recording errors and debug messages
 * @property transactionLogger The logger used for performance monitoring
 */
class EventManager internal constructor(
    private val errorLogger: ErrorLogger = FirebaseErrorLogger(),
    private val transactionLogger: TransactionLogger = FirebaseTransactionLogger()) {

    constructor(): this(FirebaseErrorLogger(), FirebaseTransactionLogger())

    companion object {
        const val STATE_KEY_LOCATION_ERROR = "location-error"
        const val STATE_KEY_LOCATION = "location"
    }


    private val eventPlugins = ArrayList<EventPlugin>()

    init {
        Logger.i("Event Manager Initialized", tag = "EventManager")
    }

    /**
     * Start a new transaction.
     *
     * The Transaction will be sent to the [TransactionLogger].
     * Note that this method makes no effort to determine if the
     * transaction has been previously started or stopped. All checking and
     * verification is the responsibility of the [TransactionLogger].
     *
     * @param name - The name of the transaction stop.
     */
    fun startTransaction(name: String) {
        transactionLogger.startTransaction(name)
    }

    /**
     * Stop/Complete a transaction.
     *
     * The Transaction will be sent to the [TransactionLogger].
     * Note that this method makes no effort to determine if the
     * transaction has been previously started. All checking and
     * verification is the responsibility of the [TransactionLogger].
     *
     * @param name - The name of the transaction to stop.
     */
    fun stopTransaction(name: String) {
        transactionLogger.stopTransaction(name)
    }

//    /**
//     * Update user attributes
//     *
//     * @param attributes - attributes that will be updated
//     */
//    fun updateUserAttributes(attributes:UserAttributes) {
//        for (eventPlugin in eventPlugins) {
//            eventPlugin.updateUserAttributes(attributes)
//        }
//    }

    /**
     * Set the userId for all errors, transactions, and events.
     *
     * When a user logs in, you can call this method to send a user identifier
     * on any registered [EventPlugin]. If an event tracking service needs or
     * can use a user id, it will be sent to the service's [EventPlugin] when
     * this method is called.
     *
     * The UserID is also sent to the [ErrorLogger].
     *
     * @param userId - A String that uniquely identifies the user (like a camID).
     * @param propertes - A Bundle of user properties that a plugin might use to categorize the user.
     *   DO NOT PUT PERSONALLY IDENTIFIABLE INFORMATION (PII) IN THIS BUNDLE.
     */
    fun setUserId(userId: String) {
        errorLogger.setUserId(userId)
        for (eventPlugin in eventPlugins) {
            eventPlugin.setUserId(userId)
        }
    }

    /**
     * Used by the [TransactionLogger] for logging performance related data.
     */
    fun incrementPerformanceEvent(transactionName: String, metricName: String, increment: Long) {
        transactionLogger.incrementPerformanceEvent(transactionName, metricName, increment)
    }

    /**
     * Used by the [TransactionLogger] for logging performance related data.
     */
    fun logPerformanceAttribute(transactionName: String, attributeName: String, attribute: String) {
        transactionLogger.logPerformanceAttribute(transactionName, attributeName, attribute)
    }

    /**
     * Used by the [TransactionLogger] for logging performance related data.
     */
    fun removePerformanceAttribute(transactionName: String, attributeName: String) {
        transactionLogger.removePerformanceAttribute(transactionName, attributeName)
    }

    /**
     * Register an [EventPlugin] to receive events logged via [logEvent]
     *
     * Once registered, the plugin will receive
     * all events that are sent via [logEvent], and the plugin can decided
     * whether or not it is interested in the event.
     *
     * @param eventPlugin - An [EventPlugin] that has been created for a single third party event tracking/logging service.
     */
    fun registerPlugin(eventPlugin: EventPlugin) {
        if(eventPlugins.find { it::class == eventPlugin::class } == null) {
            eventPlugins.add(eventPlugin)
            logMessage("Registered Event Plugin: ${eventPlugin::class.simpleName}")
        }
    }

    /**
     * Send an event to all registered [EventPlugin]s for processing.
     *
     * The event will be sent to all registered plugins.
     * If no plugins reported that they logged the event, then an exception will
     * be logged with the [ErrorLogger]. This is to facilitate notification to the
     * development teams that the event is no longer logged, and the code which
     * calls this method can likely be removed.
     *
     * @param event - An [AnalyticsEvent] to be logged by one or more event plugins.
     */
    fun logEvent(event: AnalyticsEvent) {
        var logged = false
        for (eventPlugin in eventPlugins) {
            logged = eventPlugin.logEvent(event) || logged
        }

        if (!logged) {
            val exception = Exception("Event was not logged by any plugins...")
            logException(exception, LoggableExceptionTypes.NO_PLUGIN, hashMapOf("eventType" to event.type, "eventName" to event::class.simpleName))
        }
    }

    /**
     * Send a screen view "event" to all registered [EventPlugin]s for processing.
     *
     * Some services may treat screen views differently than standard "events". Calling this
     * method to log screen views will give the [EventPlugin] for those services a chance to handle
     * them appropriately for the service that the Plugin implements.
     *
     * The Screen view will be sent to all registered plugins.
     * If no plugins report that the screen view was logged, then an exception will be
     * logged with the [ErrorLogger]. This is to facilitate notification to the
     * development teams that the view is no longer logged, and the code which
     * calls this method can likely be removed.
     *
     * @param screenName - The name of the Composable/View for the screen being logged (eg "TeeTimesListScreen")
     * @param screenClass - A display name for the screen being logged (eg "TeeTimes List")
     */
    fun logScreenView(screen: AnalyticsScreen, type: ScreenType = ScreenType.SCREEN) {
        // Don't log the "None" screen
        if (screen is AnalyticsScreen.None) return

        var logged = false

        for (screenTracker in eventPlugins) {
            logged = logged || screenTracker.logScreenView(screen, type)
        }

        //Log the screen view for firebase crashlytics usages
        logMessage("Viewed Screen: ${screen.name}")

        if (!logged) {
            val exception = Exception("Screen View was not logged by any plugins...")
            logException(exception, LoggableExceptionTypes.NO_PLUGIN, hashMapOf("screenName" to screen.name, "extras" to screen.parameters.toString()))
        }
    }

    /**
     * Remove an `EventPlugin` that was previously registered to receive events.
     *
     * @param eventPlugin - The [EventPlugin] to be removed.
     */
    fun removePlugin(eventPlugin: EventPlugin) {
        eventPlugins.remove(eventPlugin)
        logMessage("Unregistered Event Plugin: ${eventPlugin::class.simpleName}")
    }

    /**
     * Log a [LoggableException] with the [ErrorLogger].
     * This method will only write to the log if the
     * [LoggableException] is an instance of [Throwable].
     *
     * @param exception A Throwable that implements [LoggableException]
     * @param extraData An optional HashMap containing extra data to be logged with the exception
     */
    @HiddenFromObjC
    fun logException(exception: LoggableException, extraData: HashMap<String, Any?>? = null) {
        (exception as? Throwable)?.let { throwable ->
            val keys: HashMap<String, Any?> = hashMapOf()
            extraData?.let { keys.putAll(it) }
            keys.putAll(exception.getLogData())

            logException(throwable, exception.loggableType, keys)
        }
    }

    /**
     * Log a Throwable with the [ErrorLogger]
     *
     * @param throwable A Throwable that should be logged
     * @param errorType A [LoggableExceptionTypes] that defines the type of issue being logged.
     * @param data An optional HashMap containing extra data to be logged with the exception
     */
    @HiddenFromObjC
    fun logException(throwable: Throwable, errorType: LoggableExceptionTypes, data: HashMap<String, Any?>? = null) {
        val keys: HashMap<String, Any?> = hashMapOf()
        data?.let { keys.putAll(it) }
        keys["exception-type"] = errorType.displayName
        recordStateValues(keys)

        errorLogger.logException(throwable)
    }

    private fun recordStateValues(data: HashMap<String, Any?>?, tag: String? = null) {
        data?.forEach { (key, value) -> recordStateValue(key, value.toString() ) }

        if (tag != null) {
            recordStateValue("tag", tag)
        }
    }

    /**
     * Log a simple message with the [ErrorLogger]
     *
     * @param message - The message to log.
     */
    fun logMessage(message: String) {
        errorLogger.logMessage(message)
    }

    /**
     * Records a state value to the error logger.
     * When errors are logged, the current state values will be sent with the error,
     * as a set of key/value pairs.
     *
     * @param key - A key to identify the value
     * @param value - A String with a value for the key
     */
    fun recordStateValue(key: String, value: String) {
        errorLogger.recordStateValue(key, value)
    }

    /**
     * Records a state value to the error logger.
     * When errors are logged, the current state values will be sent with the error,
     * as a set of key/value pairs.
     *
     * @param key - A key to identify the value
     * @param value - A Boolean with a value for the key
     */
    fun recordStateValue(key: String, value: Boolean) {
        errorLogger.recordStateValue(key, value)
    }

    /**
     * Records a state value to the error logger.
     * When errors are logged, the current state values will be sent with the error,
     * as a set of key/value pairs.
     *
     * @param key - A key to identify the value
     * @param value - A Double with a value for the key
     */
    fun recordStateValue(key: String, value: Double) {
        errorLogger.recordStateValue(key, value)
    }

    /**
     * Records a state value to the error logger.
     * When errors are logged, the current state values will be sent with the error,
     * as a set of key/value pairs.
     *
     * @param key - A key to identify the value
     * @param value - An Int with a value for the key
     */
    fun recordStateValue(key: String, value: Int) {
        errorLogger.recordStateValue(key, value)
    }

    /**
     * Records a state value to the error logger.
     * When errors are logged, the current state values will be sent with the error,
     * as a set of key/value pairs.
     *
     * @param key - A key to identify the value
     * @param value - A Float with a value for the key
     */
    fun recordStateValue(key: String, value: Float) {
        errorLogger.recordStateValue(key, value)
    }

    /**
     * Records a state value to the error logger.
     * When errors are logged, the current state values will be sent with the error,
     * as a set of key/value pairs.
     *
     * @param key - A key to identify the value
     * @param value - A Long with a value for the key
     */
    fun recordStateValue(key: String, value: Long) {
        errorLogger.recordStateValue(key, value)
    }

}