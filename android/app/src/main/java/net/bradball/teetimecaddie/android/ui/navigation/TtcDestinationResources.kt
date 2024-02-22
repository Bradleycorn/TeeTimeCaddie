package net.bradball.teetimecaddie.android.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.features.teetimes.TTR


interface DestinationResources {
    val fabIcon: ImageVector?
    val fabIconDescriptionId: Int?
}

interface TopLevelDestinationResources: DestinationResources {
    val selectedIcon: ImageVector
    val unselectedIcon: ImageVector
    val iconTextId: Int
    val titleTextId: Int
}

sealed class TtcDestinationResources: DestinationResources {
    data object TEE_TIMES : TtcDestinationResources(), TopLevelDestinationResources {
        override val selectedIcon = TtcIcons.TeeBallClock
        override val unselectedIcon = TtcIcons.TeeBallClock
        override val iconTextId = TTR.strings.tee_times_title.resourceId
        override val titleTextId = TTR.strings.tee_times_title.resourceId
        override val fabIcon: ImageVector = TtcIcons.Add
        override val fabIconDescriptionId: Int = TTR.strings.add_tee_time.resourceId
    }
}