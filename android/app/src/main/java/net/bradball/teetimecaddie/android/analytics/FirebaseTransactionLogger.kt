package net.bradball.teetimecaddie.android.analytics

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import net.bradball.teetimecaddie.core.analytics.TransactionLogger

/**
 * Android [TransactionLogger] implementation backed by the native Firebase Performance SDK.
 *
 * This is the platform-provided binding for performance tracing. It is created by the app and
 * passed into the SDK during initialization, keeping the shared code free of any Firebase dependency.
 */
class FirebaseTransactionLogger : TransactionLogger {

    private val traces = hashMapOf<String, Trace>()

    override fun startTransaction(name: String) {
        val trace = FirebasePerformance.getInstance().newTrace(name)
        traces[name] = trace
        trace.start()
    }

    override fun stopTransaction(name: String) {
        traces[name]?.stop()
        traces.remove(name)
    }

    override fun incrementPerformanceEvent(transactionName: String, metricName: String, increment: Long) {
        traces[transactionName]?.incrementMetric(metricName, increment)
    }

    override fun logPerformanceAttribute(transactionName: String, attributeName: String, attribute: String) {
        traces[transactionName]?.putAttribute(attributeName, attribute)
    }

    override fun removePerformanceAttribute(transactionName: String, attributeName: String) {
        traces[transactionName]?.removeAttribute(attributeName)
    }
}
