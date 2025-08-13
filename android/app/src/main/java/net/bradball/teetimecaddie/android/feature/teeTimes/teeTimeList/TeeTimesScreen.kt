package net.bradball.teetimecaddie.android.feature.teeTimes.teeTimeList

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import net.bradball.teetimecaddie.android.R
import net.bradball.teetimecaddie.android.feature.teeTimes.common.TeeTimeCard
import net.bradball.teetimecaddie.android.feature.teeTimes.teeTimeEntry.TeeTimeEntryRoute
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.appBars.TtcAppBar
import net.bradball.teetimecaddie.android.ui.common.ContentLoadingIndicator
import net.bradball.teetimecaddie.android.ui.common.EmptyContent
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.navigation.TtcDestinationResources
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.core.models.TeeTime
import net.bradball.teetimecaddie.core.models.previewTeeTimeList
import net.bradball.teetimecaddie.features.teetimes.TTR

@Composable
fun TeeTimesRoute(viewModel: TeeTimesViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TeeTimesScreen(uiState)
}

@Composable
fun TeeTimesScreen(uiState: TeeTimesUiState) {

    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val composableScope = rememberCoroutineScope()

    Scaffold(
        topBar = { TtcAppBar(resources = TtcDestinationResources.TEE_TIMES) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showBottomSheet = true }) {
                Icon(TtcIcons.Add, contentDescription = "Add Tee Time")
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
                        icon = painterResource(R.drawable.ic_empty_tee),
                        modifier = Modifier
                            .padding(top = 128.dp)
                            .fillMaxWidth()
                    )

                    is TeeTimesUiState.Content -> TeeTimesList(uiState.teeTimes)
                }
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                sheetState = bottomSheetState,
                onDismissRequest = { showBottomSheet = false }
            ) {
                TeeTimeEntryRoute(onTeeTimeAdded = {
                    composableScope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                })
            }
        }
    }
}

@Composable
private fun TeeTimesList(teeTimes: List<TeeTime>) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        itemsIndexed(teeTimes) { index, teeTime ->
            if (index > 0) { Spacer(modifier = Modifier.height(8.dp)) }
            TeeTimeCard(
                teeTime = teeTime,
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(name="Content State", showSystemUi = true)
@Composable
fun TeeTimesScreenPreview() {
    MyApplicationTheme {
        TeeTimesScreen(TeeTimesUiState.Content(previewTeeTimeList))
    }
}

@Preview(name="Empty State", showSystemUi = true)
@Composable
fun EmptyTeeTimesScreenPreview() {
    MyApplicationTheme {
        TeeTimesScreen(TeeTimesUiState.Empty)
    }
}

@Preview(name="Loading State", showSystemUi = true)
@Composable
fun LoadingTeeTimesScreenPreview() {
    MyApplicationTheme {
        TeeTimesScreen(TeeTimesUiState.Loading)
    }
}