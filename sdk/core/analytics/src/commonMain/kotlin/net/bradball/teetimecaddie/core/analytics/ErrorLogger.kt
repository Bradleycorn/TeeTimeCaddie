package net.bradball.teetimecaddie.core.analytics

/**
 * Defines an interface for logging errors.
 *
 * An Error Logger is responsible for logging errors and log messages
 * to a third party service. The logger is provided to the [EventManager]
 * and will receive exceptions and log messages from it as they
 * are fired in the application.
 *
 * Unlike [EventPlugin]s, only one instance of the EventLogger is registered
 * with the [EventManager]. This interface serves as an abstraction to allow
 * us to swap out Event Logging services without having to touch other code.
 */
internal interface ErrorLogger {
    fun logException(throwable: Throwable)
    fun logMessage(message: String)

    fun recordStateValue(key: String, value: String)
    fun recordStateValue(key: String, value: Boolean)
    fun recordStateValue(key: String, value: Double)
    fun recordStateValue(key: String, value: Int)
    fun recordStateValue(key: String, value: Float)
    fun recordStateValue(key: String, value: Long)

    fun setUserId(userId: String)
}