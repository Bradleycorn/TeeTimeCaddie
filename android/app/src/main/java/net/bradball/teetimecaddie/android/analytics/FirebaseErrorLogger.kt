package net.bradball.teetimecaddie.android.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import net.bradball.teetimecaddie.core.analytics.ErrorLogger

/**
 * Android [ErrorLogger] implementation backed by the native Firebase Crashlytics SDK.
 *
 * This is the platform-provided binding for error logging. It is created by the app and passed
 * into the SDK during initialization, keeping the shared code free of any Firebase dependency.
 */
class FirebaseErrorLogger : ErrorLogger {

    private val crashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()

    override fun logException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun logMessage(message: String) {
        crashlytics.log(message)
    }

    override fun recordStateValue(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    override fun recordStateValue(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }

    override fun recordStateValue(key: String, value: Double) {
        crashlytics.setCustomKey(key, value)
    }

    override fun recordStateValue(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    override fun recordStateValue(key: String, value: Float) {
        crashlytics.setCustomKey(key, value)
    }

    override fun recordStateValue(key: String, value: Long) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }
}
