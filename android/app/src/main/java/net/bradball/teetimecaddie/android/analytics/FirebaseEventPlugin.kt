package net.bradball.teetimecaddie.android.analytics

import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventPlugin

class FirebaseEventPlugin: EventPlugin {
    override fun logEvent(event: AnalyticsEvent): Boolean {

        val params = event.asMap.toList().toTypedArray()

        Firebase.analytics.logEvent(event.name, bundleOf(*params))
        return true
    }

    override fun setUserId(userId: String) {
        Firebase.analytics.setUserId(userId)
    }

    override fun logScreenView(screenName: String, extras: Map<String, String>): Boolean {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
            extras.forEach { (key, value) -> param(key, value) }
        }

        return true
    }

}