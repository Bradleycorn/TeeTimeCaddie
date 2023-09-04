package net.bradball.teetimecaddie.core.analytics.firebase

import dev.gitlive.firebase.perf.metrics.Trace

internal actual fun FirebaseTransactionLogger.addPerfAttribute(trace: Trace, attribute: String, value: String) {
    trace.putAttribute(attribute, value)
}

internal actual fun FirebaseTransactionLogger.removePerfAttribute(trace: Trace, attribute: String) {
    trace.removeAttribute(attribute)
}
