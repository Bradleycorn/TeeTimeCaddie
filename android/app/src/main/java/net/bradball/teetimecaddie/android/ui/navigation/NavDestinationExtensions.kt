package net.bradball.teetimecaddie.android.ui.navigation

import androidx.navigation.NavDestination
import net.bradball.teetimecaddie.android.feature.teeTimes.navigation.teeTimesListRoute

val NavDestination.resources: TtcDestinationResources?
    get() = when (route) {
        teeTimesListRoute -> TtcDestinationResources.TEE_TIMES
        else -> null
    }

val NavDestination.topLevelResources: TopLevelDestinationResources?
    get() = resources as? TopLevelDestinationResources

val NavDestination.isTopLevel: Boolean
    get() = topLevelResources != null