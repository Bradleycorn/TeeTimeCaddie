package net.bradball.teetimecaddie.core.analytics

import platform.Foundation.NSError


fun EventManager.Companion.createInstance(): EventManager = EventManager()

fun EventManager.logError(error: NSError, errorType: LoggableExceptionTypes, data: Map<String, Any?>? = null) {
    val dataHash: HashMap<String, Any?> = hashMapOf()
    data?.let { dataHash.putAll(it) }

    logException(Throwable(error.localizedDescription), errorType, dataHash)
}
