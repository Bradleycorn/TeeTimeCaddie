package net.bradball.teetimecaddie.android.feature.teeTimes.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import net.bradball.teetimecaddie.android.feature.teeTimes.teeTimeList.TeeTimesListScreen
import net.bradball.teetimecaddie.android.ui.navigation.Navigator
import net.bradball.teetimecaddie.android.ui.navigation.TtcNavKey

@Serializable
data object TeeTimesListDestination: TtcNavKey

fun Navigator.navigateToTeeTimesList(clearBackStack: Boolean = false) {
    navigate(TeeTimesListDestination, clearBackStack)
}

fun EntryProviderScope<NavKey>.teeTimesEntries() {
    entry<TeeTimesListDestination> {
        TeeTimesListScreen()
    }
}