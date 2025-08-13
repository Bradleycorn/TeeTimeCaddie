package net.bradball.teetimecaddie.android.ui.common.appBars

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import net.bradball.teetimecaddie.android.ui.navigation.TopLevelDestinationResources
import net.bradball.teetimecaddie.android.ui.navigation.topLevelResources

@Composable
fun TtcAppBar(destination: NavDestination?) {
    destination?.topLevelResources?.let { resources ->
        TtcAppBar(resources)
    }
}

@Composable
fun TtcAppBar(resources: TopLevelDestinationResources) {
    TtcCenteredTopAppBar(title = stringResource(id = resources.titleTextId))
}