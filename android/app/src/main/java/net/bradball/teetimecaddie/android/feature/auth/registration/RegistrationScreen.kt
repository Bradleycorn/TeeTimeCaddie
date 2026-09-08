package net.bradball.teetimecaddie.android.feature.auth.registration

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen


@Composable
fun RegistrationScreen(
    onLoginClick: () -> Unit,
    onRegistrationComplete: ()->Unit,
    viewModel: RegistrationViewModel = hiltViewModel()) {

    Screen(AnalyticsScreen.Registration("RegistrationScreen")) {
        RegistrationContent()
    }
}

@Composable
private fun RegistrationContent() {
    Text("Registration Placeholder")
}

@Composable
@Preview
fun RegistrationScreenPreview() {
    MyApplicationTheme {
        RegistrationContent()
    }
}