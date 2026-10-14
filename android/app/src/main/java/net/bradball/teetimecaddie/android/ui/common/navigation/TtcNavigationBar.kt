package net.bradball.teetimecaddie.android.ui.common.navigation

import android.content.res.Configuration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.navigation.TopLevelDestination

/**
 * The app's bottom tab bar.
 *
 * A stock Material 3 [NavigationBar] with **no colour overrides** — its default container is
 * already `surfaceContainer`, which is the rung the theme designates for it. This component exists
 * only so the item loop over [TopLevelDestination] is written once.
 *
 * @param destinations The tabs to show, in order.
 * @param selected The tab currently showing.
 * @param onSelect Invoked when a tab is tapped, including the one already selected.
 */
@Composable
fun TtcNavigationBar(
    destinations: List<TopLevelDestination>,
    selected: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        destinations.forEach { destination ->
            val label = stringResource(destination.iconTextId)
            NavigationBarItem(
                selected = destination == selected,
                onClick = { onSelect(destination) },
                icon = {
                    Icon(
                        painter = destination.icon.painter,
                        contentDescription = label,
                    )
                },
                label = { Text(label) },
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcNavigationBarPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            TtcNavigationBar(
                destinations = TopLevelDestination.entries,
                selected = TopLevelDestination.TEE_TIMES,
                onSelect = {},
            )
        }
    }
}

@Preview(name = "Profile selected - Light", showBackground = true)
@Preview(
    name = "Profile selected - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TtcNavigationBarProfilePreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            TtcNavigationBar(
                destinations = TopLevelDestination.entries,
                selected = TopLevelDestination.PROFILE,
                onSelect = {},
            )
        }
    }
}
