package net.bradball.teetimecaddie.android.feature.teeTimes.addTeeTime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.core.models.GR
import net.bradball.teetimecaddie.features.teetimes.TTR

@Composable
fun AddTeeTimeScreen(
    onBack: () -> Unit,
    onTeeTimeCreated: () -> Unit,
    viewModel: AddTeeTimeViewModel = hiltViewModel()
) {

    Screen(AnalyticsScreen.AddTeeTime("AddTeeTimeScreen")) {
        AddTeeTimeContent(
            onBackClick = onBack
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTeeTimeContent(onBackClick: () -> Unit = {}) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(TTR.strings.add_tee_time.resourceId)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            TtcIcons.ARROW_BACK.painter,
                            contentDescription = stringResource(GR.strings.back.resourceId)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Add Tee Time Placeholder")
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun AddTeeTimeContentPreview() {
    MyApplicationTheme {
        AddTeeTimeContent()
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTeeTimeContentWithTimesPreview() {
    MyApplicationTheme {
        AddTeeTimeContent()
    }
}
