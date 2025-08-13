package net.bradball.teetimecaddie.android.feature.teeTimes.navigation

import androidx.compose.material3.SnackbarResult
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.google.accompanist.navigation.material.bottomSheet
import net.bradball.teetimecaddie.android.feature.teeTimes.teeTimeEntry.TeeTimeEntryRoute
import net.bradball.teetimecaddie.android.feature.teeTimes.teeTimeList.TeeTimesRoute
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.navigation.clearBackStack

const val teeTimesGraphRoute = "teetimes_graph"
const val teeTimesListRoute = "teetimes_list"
const val addTeeTimeRoute = "add_teetime"

fun NavHostController.navigateToTeeTimes(clearBackStack: Boolean) {
    val options = clearBackStack(navHostController = this)
    navigateToTeeTimes(options)
}

fun NavHostController.navigateToTeeTimes(navOptions: NavOptions? = null) {
    this.navigate(teeTimesGraphRoute, navOptions)
}

fun NavHostController.showAddTeeTimeSheet() {
    this.navigate(addTeeTimeRoute)
}

fun NavGraphBuilder.teeTimesGraph(
    nestedDestinations: NavGraphBuilder.()->Unit = {}
) {
    navigation(
        route = teeTimesGraphRoute,
        startDestination = teeTimesListRoute
    ) {
        composable(route = teeTimesListRoute) {
            TeeTimesRoute()
        }

        nestedDestinations()
    }
}
