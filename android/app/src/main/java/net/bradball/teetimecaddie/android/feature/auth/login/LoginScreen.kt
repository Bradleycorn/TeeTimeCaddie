package net.bradball.teetimecaddie.android.feature.auth.login

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen


@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()) {

    Screen(AnalyticsScreen.Login("LoginScreen")) {
        LoginContent()
    }
}

@Composable
private fun LoginContent() {
    Text("Login Screen Placeholder")
}


@Composable
@Preview
fun LoginScreenPreview() {
    MyApplicationTheme {
        LoginContent()
    }
}