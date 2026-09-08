package net.bradball.teetimecaddie.android.feature.teeTimes.teeTimesList

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.appBars.TtcCenteredTopAppBar
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.core.models.TeeTime
import net.bradball.teetimecaddie.features.teetimes.TTR

@Composable
fun TeeTimesListScreen(
    onAddTeeTimeClick: () -> Unit,
    onTeeTimeClick: (String) -> Unit,
    viewModel: TeeTimesListViewModel = hiltViewModel()
) {
    Screen(AnalyticsScreen.TeeTimeList("TeeTimesListScreen")) {
        TeeTimesContent(
            onAddTeeTimeClick = onAddTeeTimeClick,
            onTeeTimeClick = onTeeTimeClick
        )
    }
}

@Composable
fun TeeTimesContent(
    onAddTeeTimeClick: () -> Unit = {},
    onTeeTimeClick: (String) -> Unit = {}
) {

    Scaffold(
        topBar = { TtcCenteredTopAppBar(stringResource(TTR.strings.tee_times_title.resourceId)) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTeeTimeClick) {
                Icon(TtcIcons.ADD.painter, contentDescription = "Add Tee Time")
            }
        }
    ) { insets ->
        Column(modifier = Modifier.padding(insets)) {
            Text("Tee Times List Placeholder")
        }
    }
}


@Preview(name="Content State", showSystemUi = true)
@Composable
fun TeeTimesScreenPreview() {
    MyApplicationTheme {
        TeeTimesContent()
    }
}

@Preview(name="Empty State", showSystemUi = true)
@Composable
fun EmptyTeeTimesScreenPreview() {
    MyApplicationTheme {
        TeeTimesContent()
    }
}

@Preview(name="Loading State", showSystemUi = true)
@Composable
fun LoadingTeeTimesScreenPreview() {
    MyApplicationTheme {
        TeeTimesContent()
    }
}