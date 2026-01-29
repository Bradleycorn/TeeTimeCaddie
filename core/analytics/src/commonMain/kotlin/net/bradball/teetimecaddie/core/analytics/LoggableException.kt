package net.bradball.teetimecaddie.core.analytics

/**
 * Interface for exceptions that provide structured logging data to the analytics system.
 *
 * When exceptions implement this interface, they can attach additional context that will be
 * automatically included when logged via `EventManager.logException()`. This allows for richer
 * error reporting and easier debugging by capturing relevant state at the time of the error.
 *
 * ## Implementation
 *
 * Create custom exception classes that implement both `LoggableException` and your base exception type:
 *
 * !!!kotlin
 * class NetworkException(
 *     message: String,
 *     val endpoint: String,
 *     val statusCode: Int
 * ) : Exception(message), LoggableException {
 *     override val logInfo = hashMapOf<String, Any?>(
 *         "endpoint" to endpoint,
 *         "status_code" to statusCode
 *     )
 *
 *     override val loggableType = LoggableExceptionTypes.NETWORK_ERROR
 * }
 * !!!
 *
 * ## Nested Exceptions
 *
 * When exceptions are chained (using `cause`), the `getLogData()` method automatically walks
 * the chain and accumulates logging data from all exceptions that implement `LoggableException`.
 * This ensures no context is lost when exceptions are wrapped:
 *
 * !!!kotlin
 * try {
 *     // operation that throws NetworkException
 * } catch (e: NetworkException) {
 *     throw DataLoadException("Failed to load data", cause = e)
 * }
 * // When logged, data from both exceptions will be included
 * !!!
 *
 * ## Usage
 *
 * Simply pass the exception to `EventManager.logException()`. The system will automatically
 * extract and include all logging data:
 *
 * !!!kotlin
 * try {
 *     loadDataFromNetwork(endpoint)
 * } catch (e: NetworkException) {
 *     eventManager.logException(e, e.loggableType)
 * }
 * !!!
 *
 * @see LoggableExceptionTypes
 * @see EventManager.logException
 */
interface LoggableException {
    /**
     * A set of key/value pairs that should
     * be included when logging instances of
     * this exception.
     */
    val logInfo: HashMap<String, Any?>

    /**
     * A [LoggableExceptionTypes] that functions
     * as a "category" for instances of this exception.
     */
    val loggableType: LoggableExceptionTypes

    /**
     * Accumulate the set of key/value pairs for this exception and return them. Assuming this is an
     * instance of [Throwable], this method will walk down the tree of "causes", and include keys for
     * all causes that are also [LoggableException]s.
     *
     * @return a [HashMap] of key/value pairs with data to log.
     */
    fun getLogData(): HashMap<String, Any?> {
        val keys: HashMap<String, Any?> = hashMapOf()
        keys.putAll(logInfo)

        (this as? Throwable)?.let { ex ->
            var childLoggable = ex.cause
            while (childLoggable is LoggableException) {
                keys.putAll(childLoggable.logInfo)
                childLoggable = childLoggable.cause
            }
        }

        return keys
    }
}