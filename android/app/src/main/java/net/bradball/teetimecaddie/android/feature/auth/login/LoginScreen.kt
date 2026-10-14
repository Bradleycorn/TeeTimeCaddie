package net.bradball.teetimecaddie.android.feature.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bradball.teetimecaddie.android.feature.auth.common.AuthBrandLockup
import net.bradball.teetimecaddie.android.feature.auth.common.AuthLegalFooter
import net.bradball.teetimecaddie.android.feature.auth.common.AuthMessage
import net.bradball.teetimecaddie.android.feature.auth.common.AuthMessageBlock
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.theme.TtcColorRole
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.appBars.TtcTopAppBar
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcButton
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcOutlinedButton
import net.bradball.teetimecaddie.android.ui.common.forms.TtcPasswordField
import net.bradball.teetimecaddie.android.ui.common.forms.TtcTextField
import net.bradball.teetimecaddie.android.ui.common.forms.TtcTextFieldDefaults
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.preview.ScreenPreviews
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.features.auth.AR

/**
 * The credentials screen: one email and one password, serving both signing in and starting an
 * account.
 *
 * Which of the two happens is decided by the button that is tapped. There is no mode to switch and
 * no second form — the profile step only appears once the account exists.
 *
 * @param onCreateAccount Invoked with the typed address once the account has been created.
 */
@Composable
fun LoginScreen(
    onCreateAccount: (email: String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.AccountCreated -> onCreateAccount(event.email)
            }
        }
    }

    Screen(AnalyticsScreen.Login("LoginScreen")) {
        LoginContent(
            uiState = uiState,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onSignIn = viewModel::signIn,
            onCreateAccount = viewModel::createAccount,
            onMessageAction = viewModel::onMessageAction,
        )
    }
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onCreateAccount: () -> Unit,
    onMessageAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    // The address is what someone types first, so the screen arrives ready for it.
    LaunchedEffect(Unit) { emailFocus.requestFocus() }

    Scaffold(
        modifier = modifier,
        topBar = { TtcTopAppBar() },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = LoginDefaults.HorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LoginDefaults.Spacing),
        ) {
            Spacer(Modifier.padding(top = LoginDefaults.TopSpacing))

            AuthBrandLockup()

            // Between the lockup and the fields: close enough to the fields to read as being about
            // them, far enough not to be mistaken for part of the branding.
            uiState.message?.let { message ->
                AuthMessageBlock(message = message, onAction = onMessageAction)
            }

            TtcTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = stringResource(AR.strings.field_label_email.resourceId),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocus),
                leadingIcon = TtcIcons.EMAIL,
                enabled = !uiState.isSubmitting,
                keyboardOptions = TtcTextFieldDefaults.EmailKeyboardOptions
                    .copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
            )

            TtcPasswordField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocus),
                enabled = !uiState.isSubmitting,
                keyboardOptions = TtcTextFieldDefaults.PasswordKeyboardOptions
                    .copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboard?.hide()
                        onSignIn()
                    }
                ),
            )

            TtcButton(
                text = stringResource(AR.strings.auth_sign_in_button.resourceId),
                onClick = onSignIn,
                modifier = Modifier.fillMaxWidth(),
                color = TtcColorRole.Primary,
                enabled = uiState.canSubmit,
                isLoading = uiState.isSubmitting,
            )

            TtcOutlinedButton(
                text = stringResource(AR.strings.auth_create_account_button.resourceId),
                onClick = onCreateAccount,
                modifier = Modifier.fillMaxWidth(),
                color = TtcColorRole.Primary,
                enabled = uiState.canSubmit,
            )

            AuthLegalFooter(modifier = Modifier.padding(top = LoginDefaults.FooterSpacing))

            Spacer(Modifier.padding(bottom = LoginDefaults.TopSpacing))
        }
    }
}

private object LoginDefaults {
    val HorizontalPadding = 24.dp
    val Spacing = 16.dp
    val TopSpacing = 16.dp
    val FooterSpacing = 8.dp
}

@ScreenPreviews
@Composable
private fun LoginContentPreview() {
    MyApplicationTheme {
        LoginContent(
            uiState = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onSignIn = {},
            onCreateAccount = {},
            onMessageAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun LoginContentSignInFailedPreview() {
    MyApplicationTheme {
        LoginContent(
            uiState = LoginUiState(
                email = "dana@example.com",
                password = "hunter2",
                message = AuthMessage.SignInFailed,
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onSignIn = {},
            onCreateAccount = {},
            onMessageAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun LoginContentEmailInUsePreview() {
    MyApplicationTheme {
        LoginContent(
            uiState = LoginUiState(
                email = "dana@example.com",
                password = "hunter2",
                message = AuthMessage.EmailInUse("dana@example.com"),
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onSignIn = {},
            onCreateAccount = {},
            onMessageAction = {},
        )
    }
}
