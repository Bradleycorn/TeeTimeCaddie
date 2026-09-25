package net.bradball.teetimecaddie.android.feature.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcOutlinedButton
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.features.auth.AR

/**
 * Placeholder credentials screen.
 *
 * TTC-80 replaces the body with the designed brand lockup, email/password fields, message block and
 * legal footer. Until then it only has to prove the shell routes correctly.
 */
@Composable
fun LoginScreen(
    onCreateAccount: (email: String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    Screen(AnalyticsScreen.Credentials("LoginScreen")) {
        LoginContent(onCreateAccount = { onCreateAccount(PLACEHOLDER_EMAIL) })
    }
}

private const val PLACEHOLDER_EMAIL = "placeholder@example.com"

@Composable
private fun LoginContent(onCreateAccount: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text("Credentials Screen Placeholder")
        TtcOutlinedButton(
            text = stringResource(AR.strings.auth_create_account_button.resourceId),
            onClick = onCreateAccount,
        )
    }
}

@Preview(name = "Light")
@Composable
private fun LoginContentPreview() {
    MyApplicationTheme {
        LoginContent(onCreateAccount = {})
    }
}
