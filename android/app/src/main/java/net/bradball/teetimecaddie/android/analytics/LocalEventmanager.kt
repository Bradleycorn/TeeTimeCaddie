package net.bradball.teetimecaddie.android.analytics

import androidx.compose.runtime.staticCompositionLocalOf
import net.bradball.teetimecaddie.core.analytics.EventManager

val LocalEventManager = staticCompositionLocalOf<EventManager> {
    EventManager()
}