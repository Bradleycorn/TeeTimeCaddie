package net.bradball.teetimecaddie.android.feature.auth.login

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import net.bradball.teetimecaddie.android.feature.auth.common.AuthScreen
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.buttons.LoadingButton
import net.bradball.teetimecaddie.android.ui.common.forms.InputSpacer
import net.bradball.teetimecaddie.android.ui.common.forms.OutlinedPasswordField
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.features.auth.AR

private const val SCREEN_NAME = "Login"

@Composable
fun LoginRoute(
    onRegisterClick: () -> Unit,
    onLoggedIn: () -> Unit,
    onShowSnackbar: suspend (String, String?)-> SnackbarResult,
    viewModel: LoginViewModel = hiltViewModel()) {

    val context = LocalContext.current

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { messageId ->
            onShowSnackbar(context.getString(messageId), null)
            viewModel.clearError()
        }
    }

    LaunchedEffect(viewModel.loginSuccess) {
        if (viewModel.loginSuccess) {
            onLoggedIn()
        }
    }

    LoginScreen(
        showLoadingSpinner = viewModel.showLoadingProgress,
        onInputChanged = viewModel::clearError,
        onLoginClick = viewModel::login,
        onRegisterClick = onRegisterClick)
}

@Composable
private fun LoginScreen(
    showLoadingSpinner: Boolean,
    onInputChanged: () -> Unit = {},
    onLoginClick: (String, String) -> Unit = { _,_ -> },
    onRegisterClick: () -> Unit = {}) {

    var email by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var password by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    AuthScreen(
        analyticsScreen = AnalyticsScreen.Login,
        title = stringResource(id = AR.strings.login_screen_title.resourceId),
        footerText = stringResource(AR.strings.login_register_prompt.resourceId),
        footerActionText = stringResource(id = AR.strings.login_register_button.resourceId),
        onFooterActionClick =  onRegisterClick
    ) {
        OutlinedTextField(
            label = { Text(stringResource(AR.strings.field_label_email.resourceId)) },
            value = email,
            onValueChange = { value ->
                email = value
                onInputChanged()
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        InputSpacer()

        OutlinedPasswordField(
            value = password,
            onValueChange = { value ->
                password = value
                onInputChanged()
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )

        InputSpacer()

        LoadingButton(
            text = stringResource(AR.strings.login_submit_button.resourceId),
            onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                onLoginClick(email.text, password.text)
            },
            modifier = Modifier.align(Alignment.End),
            enabled = email.text.isNotBlank() && password.text.isNotBlank(),
            isLoading = showLoadingSpinner
        )
    }
}


@Composable
@Preview(showSystemUi = false, device = "id:pixel_7_pro")
fun LoginScreenPreview() {
    MyApplicationTheme {
        LoginScreen(showLoadingSpinner = false)
    }
}