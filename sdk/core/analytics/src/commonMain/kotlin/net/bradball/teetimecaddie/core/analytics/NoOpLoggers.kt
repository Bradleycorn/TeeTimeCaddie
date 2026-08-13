package net.bradball.teetimecaddie.core.analytics

/**
 * Default no-op [ErrorLogger] used when no platform logger is provided.
 *
 * The real error-logging implementations are provided by the platform applications and injected
 * into the SDK during initialization. This no-op keeps [EventManager] usable (e.g. in previews,
 * tests, or before a real logger is wired up) without pulling in any error-logging dependency.
 */
class NoOpErrorLogger : ErrorLogger {
    override fun logException(throwable: Throwable) {}
    override fun logMessage(message: String) {}
    override fun recordStateValue(key: String, value: String) {}
    override fun recordStateValue(key: String, value: Boolean) {}
    override fun recordStateValue(key: String, value: Double) {}
    override fun recordStateValue(key: String, value: Int) {}
    override fun recordStateValue(key: String, value: Float) {}
    override fun recordStateValue(key: String, value: Long) {}
    override fun setUserId(userId: String) {}
}

/**
 * Default no-op [TransactionLogger] used when no platform logger is provided.
 *
 * See [NoOpErrorLogger] for the rationale.
 */
class NoOpTransactionLogger : TransactionLogger {
    override fun startTransaction(name: String) {}
    override fun stopTransaction(name: String) {}
    override fun incrementPerformanceEvent(transactionName: String, metricName: String, increment: Long) {}
    override fun logPerformanceAttribute(transactionName: String, attributeName: String, attribute: String) {}
    override fun removePerformanceAttribute(transactionName: String, attributeName: String) {}
}
