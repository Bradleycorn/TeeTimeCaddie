package net.bradball.teetimecaddie.core.analytics.firebase

import dev.gitlive.firebase.perf.metrics.Trace
import dev.gitlive.firebase.perf.metrics.ios
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
fun Trace.putAttribute(key: String, value: String) {
    ios?.setValue(value = value, forAttribute = key)
}

@OptIn(ExperimentalForeignApi::class)
fun Trace.removeAttribute(key: String) {
    ios?.removeAttribute(key)
}

internal actual fun FirebaseTransactionLogger.addPerfAttribute(trace: Trace, attribute: String, value: String) {
    trace.putAttribute(attribute, value)
}

internal actual fun FirebaseTransactionLogger.removePerfAttribute(trace: Trace, attribute: String) {
    trace.removeAttribute(attribute)
}