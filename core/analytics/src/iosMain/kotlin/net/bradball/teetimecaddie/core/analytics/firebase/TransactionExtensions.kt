package net.bradball.teetimecaddie.core.analytics.firebase

import dev.gitlive.firebase.perf.metrics.Trace

fun Trace.putAttribute(key: String, value: String) {
    ios?.setValue(value = value, forAttribute = key)
}

fun Trace.removeAttribute(key: String) {
    ios?.removeAttribute(key)
}

internal actual fun FirebaseTransactionLogger.addPerfAttribute(trace: Trace, attribute: String, value: String) {
    trace.putAttribute(attribute, value)
}

internal actual fun FirebaseTransactionLogger.removePerfAttribute(trace: Trace, attribute: String) {
    trace.removeAttribute(attribute)
}