package net.bradball.teetimecaddie.android.feature.auth.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.appBars.TtcTopAppBar
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.features.auth.AR

/**
 * Placeholder profile step of account creation.
 *
 * TTC-80 replaces the body with the name / mobile / photo form and switches [email] over to Hilt
 * assisted injection so the ViewModel owns it.
 */
@Composable
fun RegistrationScreen(
    email: String,
    onBack: () -> Unit,
    viewModel: RegistrationViewModel = hiltViewModel(),
) {
    Screen(AnalyticsScreen.ProfileSetup("RegistrationScreen")) {
        RegistrationContent(email = email, onBack = onBack)
    }
}

@Composable
private fun RegistrationContent(
    email: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TtcTopAppBar(
                title = stringResource(AR.strings.create_account_title.resourceId),
                onBack = onBack,
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            Text("Profile Setup Placeholder")
            Text(email)
        }
    }
}

@Preview(name = "Light")
@Composable
private fun RegistrationContentPreview() {
    MyApplicationTheme {
        RegistrationContent(email = "dana@example.com", onBack = {})
    }
}
