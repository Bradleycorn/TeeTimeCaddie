package net.bradball.teetimecaddie.android.feature.profile

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.theme.TtcColorRole
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.appBars.TtcCenteredTopAppBar
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcOutlinedButton
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.core.models.previewPlayer
import net.bradball.teetimecaddie.features.players.PR

/**
 * Placeholder profile screen.
 *
 * It carries only what the app shell needs to be exercised end to end — the player's name and a
 * working sign-out. TTC-80 replaces the body with the designed avatar / name / email / phone
 * layout.
 */
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Screen(AnalyticsScreen.Profile("ProfileScreen")) {
        ProfileContent(
            uiState = uiState,
            onSignOut = viewModel::signOut,
        )
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TtcCenteredTopAppBar(stringResource(PR.strings.profile_title.resourceId)) },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            when (uiState) {
                ProfileUiState.Loading -> Text("Loading profile…")
                is ProfileUiState.Content -> Text(
                    text = uiState.player.name,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            TtcOutlinedButton(
                text = stringResource(PR.strings.profile_sign_out_button.resourceId),
                icon = TtcIcons.LOGOUT,
                color = TtcColorRole.Error,
                onClick = onSignOut,
            )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileContentPreview() {
    MyApplicationTheme {
        ProfileContent(uiState = ProfileUiState.Content(previewPlayer), onSignOut = {})
    }
}

@Preview(name = "Loading")
@Composable
private fun ProfileContentLoadingPreview() {
    MyApplicationTheme {
        ProfileContent(uiState = ProfileUiState.Loading, onSignOut = {})
    }
}
