package net.bradball.teetimecaddie.core.analytics

internal interface TransactionLogger {

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
