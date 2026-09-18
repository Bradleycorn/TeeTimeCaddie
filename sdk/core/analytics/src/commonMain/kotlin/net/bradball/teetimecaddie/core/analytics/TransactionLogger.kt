package net.bradball.teetimecaddie.core.analytics

/**
 * Interface for logging performance transactions in the analytics system.
 *
 * `TransactionLogger` provides methods to track the duration and metrics of significant operations
 * in the application. Transactions help identify performance bottlenecks and monitor the health
 * of critical user flows.
 *
 * ## Usage
 *
 * Transactions are accessed through `EventManager`, which delegates to registered plugins that
 * implement this interface:
 *
 * !!!kotlin
 * // Start timing an operation
 * eventManager.startTransaction("load_user_data")
 *
 * // Perform the operation
 * try {
 *     loadDataFromNetwork()
 *     eventManager.incrementPerformanceEvent("load_user_data", "network_calls", 1)
 *     processData()
 *     eventManager.logPerformanceAttribute("load_user_data", "data_source", "cache")
 * } finally {
 *     // Always stop the transaction
 *     eventManager.stopTransaction("load_user_data")
 * }
 * !!!
 *
 * ## Implementation Notes
 *
 * Implementers of this interface must handle several edge cases:
 *
 * - **Duplicate starts**: The same transaction may be started multiple times before being stopped.
 *   Implementations should track active transactions and ignore duplicate start calls.
 *
 * - **Stopping without starting**: A transaction may be stopped without a corresponding start.
 *   Implementations should validate that a transaction exists before stopping it.
 *
 * - **Duplicate stops**: A transaction may be stopped multiple times. Implementations should
 *   ensure a transaction is only recorded once.
 *
 * ## Transaction Metrics
 *
 * In addition to timing, transactions can capture:
 * - **Performance counters**: Track how many times specific events occur during the transaction
 * - **Attributes**: Attach contextual string data to provide additional details
 *
 * @see EventManager.startTransaction
 * @see EventManager.stopTransaction
 * @see EventManager.incrementPerformanceEvent
 * @see EventManager.logPerformanceAttribute
 * @see EventManager.removePerformanceAttribute
 */
interface TransactionLogger {

    companion object { }

    /**
     * Start a transaction.
     *
     * Note that the same transaction may be passed multiple times.
     * It is up to implementers to do their own checking to prevent
     * duplicate transactions in which the same transaction is started several times
     * in a row without corresponding calls to _stopTransaction_.
     */
    fun startTransaction(name: String)

    /**
     * Stop a transaction.
     *
     * Note that the same transaction may be passed multiple times.
     * It is up to implementers to provide their own logic and checking
     * to make sure a transaction has been started before it can be stopped,
     * and/or that a transaction is not stopped multiple times.
     */
    fun stopTransaction(name: String)

    /**
     * Increment a performance counter for a transaction.
     *
     * During a transaction, there may be certain events that happen multiple times.
     * This method can be called to increment a counter to keep track of how many times
     * the event happens. For example, you might create a counter to keep track of the
     * number of network calls made during a transaction.
     *
     * @param transactionName - the name of the transaction that was created
     * @param metricName - the name of the performance event being measured
     * @param increment - what to increment the occurrences of the event by
     *
     */
    fun incrementPerformanceEvent(transactionName: String, metricName: String, increment: Long)

    /**
     * Set some data that should be logged with a transaction.
     *
     * @param transactionName - the name of the transaction that was created
     * @param attributeName - the key to access the attribute
     * @param attribute - the data being passed in
     *
     **/
    fun logPerformanceAttribute(transactionName: String, attributeName: String, attribute: String)

    /**
     * Remove a performance attribute from a transaction.
     */
    fun removePerformanceAttribute(transactionName: String, attributeName: String)
}
