package net.bradball.teetimecaddie.core.analytics

import platform.Foundation.NSError


/**
 * Swift convenience. Kotlin default arguments do not survive the ObjC export, so without this a
 * Swift caller would have to spell out `getInstance(enableLogging:)` every time.
 */
fun EventManager.Companion.createInstance(): EventManager = getInstance()

fun EventManager.logError(error: NSError, errorType: LoggableExceptionTypes, data: Map<String, Any?>? = null) {
    val dataHash: HashMap<String, Any?> = hashMapOf()
    data?.let { dataHash.putAll(it) }

    logException(Throwable(error.localizedDescription), errorType, dataHash)
}
