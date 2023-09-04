package net.bradball.teetimecaddie.core.analytics

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