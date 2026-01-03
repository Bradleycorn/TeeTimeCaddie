package net.bradball.teetimecaddie.android.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import net.bradball.teetimecaddie.android.feature.teeTimes.navigation.TeeTimesListDestination
import net.bradball.teetimecaddie.android.ui.common.icons.Icons
import net.bradball.teetimecaddie.features.teetimes.TTR

enum class TopLevelDestination (
    val icon: Icons,
    @StringRes val iconTextId: Int,
    val destination: TtcNavKey
): TtcNavKey {
    TEE_TIMES(
        icon = Icons.TEE_CLOCK,
        iconTextId = TTR.strings.tee_times_title.resourceId,
        destination = TeeTimesListDestination
    ),
}
