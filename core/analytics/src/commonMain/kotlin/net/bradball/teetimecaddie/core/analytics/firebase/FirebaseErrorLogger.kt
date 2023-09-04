package net.bradball.teetimecaddie.core.analytics.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import net.bradball.teetimecaddie.core.analytics.ErrorLogger

internal class FirebaseErrorLogger: ErrorLogger {

    override fun logException(throwable: Throwable) {
        Firebase.crashlytics.recordException(throwable)
    }

    override fun logMessage(message: String) {
        Firebase.crashlytics.log(message)
    }

    override fun recordStateValue(key: String, value: String) {
        Firebase.crashlytics.setCustomKey(key, value)
    }

    override fun recordStateValue(key: String, value: Boolean) {
        Firebase.crashlytics.setCustomKey(key, value)
    }

    override fun recordStateValue(key: String, value: Double) {
        Firebase.crashlytics.setCustomKey(key, value)
    }

    override fun recordStateValue(key: String, value: Int) {
        Firebase.crashlytics.setCustomKey(key, value)
    }

    override fun recordStateValue(key: String, value: Float) {
        Firebase.crashlytics.setCustomKey(key, value)
    }

    override fun recordStateValue(key: String, value: Long) {
        Firebase.crashlytics.setCustomKey(key, value)
    }

    override fun setUserId(userId: String) {
        Firebase.crashlytics.setUserId(userId)
    }
}