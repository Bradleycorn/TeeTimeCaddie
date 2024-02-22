package net.bradball.teetimecaddie.android.feature.teeTimes.teeTimeList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bradball.teetimecaddie.android.R
import net.bradball.teetimecaddie.android.feature.teeTimes.common.TeeTimeCard
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.ContentLoadingIndicator
import net.bradball.teetimecaddie.android.ui.common.EmptyContent
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.core.models.TeeTime
import net.bradball.teetimecaddie.core.models.previewTeeTimeList

@Composable
fun TeeTimesRoute(
    viewModel: TeeTimesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TeeTimesScreen(uiState)
}

@Composable
fun TeeTimesScreen(uiState: TeeTimesUiState) {
    Screen(AnalyticsScreen.TeeTimeList) {
        Column {
//            TopAppBar(title = { Text(text = "Tee Times") })

            when (uiState) {
                is TeeTimesUiState.Loading -> ContentLoadingIndicator()
                is TeeTimesUiState.Empty -> EmptyContent(
                    title = "Tee Sheet is Wide Open",
                    message = "You don't have any scheduled tee times.",
                    icon = painterResource(R.drawable.ic_empty_tee),
                    modifier = Modifier
                        .padding(top = 128.dp)
                        .fillMaxWidth()
                )
                is TeeTimesUiState.Content -> TeeTimesList(uiState.teeTimes)
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