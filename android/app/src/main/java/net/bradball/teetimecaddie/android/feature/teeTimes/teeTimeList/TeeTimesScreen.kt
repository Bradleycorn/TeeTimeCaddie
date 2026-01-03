package net.bradball.teetimecaddie.android.feature.teeTimes.teeTimeList

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.ContentLoadingIndicator
import net.bradball.teetimecaddie.android.ui.common.EmptyContent
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.appBars.TtcCenteredTopAppBar
import net.bradball.teetimecaddie.android.ui.common.icons.Icons
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.core.models.TeeTime
import net.bradball.teetimecaddie.core.models.previewTeeTimeList
import net.bradball.teetimecaddie.features.teetimes.TTR

@Composable
fun TeeTimesListScreen(viewModel: TeeTimesViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TeeTimesContent(uiState)
}

@Composable
fun TeeTimesContent(uiState: TeeTimesUiState) {

    Scaffold(
        topBar = { TtcCenteredTopAppBar(stringResource(TTR.strings.tee_times_title.resourceId)) },
        floatingActionButton = {
            FloatingActionButton(onClick = { TODO("Show Add tee time screen/modal") }) {
                Icon(Icons.ADD.painter, contentDescription = "Add Tee Time")
            }
        }
    ) { insets ->
        Screen(AnalyticsScreen.TeeTimeList, modifier = Modifier.padding(insets)) {
            Column {
                when (uiState) {
                    is TeeTimesUiState.Loading -> ContentLoadingIndicator()
                    is TeeTimesUiState.Empty -> EmptyContent(
                        title = stringResource(TTR.strings.empty_tee_times_title.resourceId),
                        message = stringResource(TTR.strings.empty_tee_times_message.resourceId),
                        icon = Icons.TEE.painter,
                        modifier = Modifier
                            .padding(top = 128.dp)
                            .fillMaxWidth()
                    )

                    is TeeTimesUiState.Content -> TeeTimesList(uiState.teeTimes)
                }
            }
        }
    }
}

@Composable
private fun TeeTimesList(teeTimes: List<TeeTime>) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        TODO("Show Tee Times")
    }
}

@Preview(name="Content State", showSystemUi = true)
@Composable
fun TeeTimesScreenPreview() {
    MyApplicationTheme {
        TeeTimesContent(TeeTimesUiState.Content(previewTeeTimeList))
    }
}

@Preview(name="Empty State", showSystemUi = true)
@Composable
fun EmptyTeeTimesScreenPreview() {
    MyApplicationTheme {
        TeeTimesContent(TeeTimesUiState.Empty)
    }
}

@Preview(name="Loading State", showSystemUi = true)
@Composable
fun LoadingTeeTimesScreenPreview() {
    MyApplicationTheme {
        TeeTimesContent(TeeTimesUiState.Loading)
    }
}