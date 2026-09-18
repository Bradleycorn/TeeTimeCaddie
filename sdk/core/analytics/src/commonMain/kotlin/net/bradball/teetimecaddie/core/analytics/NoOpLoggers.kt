package net.bradball.teetimecaddie.core.analytics

/**
 * Loggers that do nothing.
 *
 * [EventManager] defaults to Firebase-backed loggers, which reach `FirebaseCrashlytics` as soon as
 * they are constructed and therefore need a live `FirebaseApp`. That makes an `EventManager` —
 * and anything that takes one — impossible to build in a plain unit test. Passing these instead
 * keeps the event surface intact while sending it nowhere.
 */
object NoOpErrorLogger : ErrorLogger {
    override fun logException(throwable: Throwable) = Unit
    override fun logMessage(message: String) = Unit
    override fun recordStateValue(key: String, value: String) = Unit
    override fun recordStateValue(key: String, value: Boolean) = Unit
    override fun recordStateValue(key: String, value: Double) = Unit
    override fun recordStateValue(key: String, value: Int) = Unit
    override fun recordStateValue(key: String, value: Float) = Unit
    override fun recordStateValue(key: String, value: Long) = Unit
    override fun setUserId(userId: String) = Unit
}

object NoOpTransactionLogger : TransactionLogger {
    override fun startTransaction(name: String) = Unit
    override fun stopTransaction(name: String) = Unit
    override fun incrementPerformanceEvent(transactionName: String, metricName: String, increment: Long) = Unit
    override fun logPerformanceAttribute(transactionName: String, attributeName: String, attribute: String) = Unit
    override fun removePerformanceAttribute(transactionName: String, attributeName: String) = Unit
}
