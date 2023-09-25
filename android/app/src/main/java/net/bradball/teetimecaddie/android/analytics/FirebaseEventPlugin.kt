package net.bradball.teetimecaddie.android.analytics

import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.core.analytics.EventPlugin
import net.bradball.teetimecaddie.core.analytics.ScreenType

class FirebaseEventPlugin: EventPlugin {
    override fun logEvent(event: AnalyticsEvent): Boolean {

        val params = event.asMap.toList().toTypedArray()

        Firebase.analytics.logEvent(event.name, bundleOf(*params))
        return true
    }

    override fun setUserId(userId: String) {
        Firebase.analytics.setUserId(userId)
    }

    override fun logScreenView(screen: AnalyticsScreen, displayMethod: ScreenType): Boolean {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screen.name)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, displayMethod.displayName)
            screen.parameters.forEach { (key, value) -> param(key, value) }
        }

        return true
    }

}