package net.bradball.teetimecaddie.core.analytics.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.perf.metrics.Trace
import dev.gitlive.firebase.perf.performance
import net.bradball.teetimecaddie.core.analytics.TransactionLogger

internal expect fun FirebaseTransactionLogger.addPerfAttribute(trace: Trace, attribute: String, value: String)
internal expect fun FirebaseTransactionLogger.removePerfAttribute(trace: Trace, attribute: String)

internal class FirebaseTransactionLogger: TransactionLogger {

    private val traces = hashMapOf<String, Trace>()

    override fun startTransaction(name: String) {
        traces[name] = Firebase.performance.newTrace(name)
        traces[name]?.start()
    }

    override fun stopTransaction(name: String) {
        traces[name]?.stop()
        traces.remove(name)
    }

    override fun incrementPerformanceEvent(transactionName: String, metricName: String, increment: Long) {
        traces[transactionName]?.incrementMetric(metricName, increment)
    }

    override fun logPerformanceAttribute(transactionName: String, attributeName: String, attribute: String) {
        traces[transactionName]?.let { trace ->
            addPerfAttribute(trace, attributeName, attribute)
        }
    }

    override fun removePerformanceAttribute(transactionName: String, attributeName: String) {
        traces[transactionName]?.let { trace ->
            removePerfAttribute(trace, attributeName)
        }
    }
}

