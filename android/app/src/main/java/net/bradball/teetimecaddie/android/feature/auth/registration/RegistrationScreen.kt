package net.bradball.teetimecaddie.android.feature.auth.registration

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
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

private const val SCREEN_NAME = "Registration"

@Composable
fun RegistrationRoute(
    onLoginClick: () -> Unit,
    onRegistrationComplete: ()->Unit,
    viewModel: RegistrationViewModel = hiltViewModel()) {

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { messageId ->
            snackbarHostState.showSnackbar(
                message = context.getString(messageId),
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
           viewModel.clearError()
        }
    }

    LaunchedEffect(viewModel.registrationSuccess) {
        if (viewModel.registrationSuccess) {
            onRegistrationComplete()
        }
    }

    RegistrationScreen(
        showLoadingAnimation = viewModel.isProcessingRegistration,
        onInputChanged = viewModel::clearError,
        onCreateAccountClick = viewModel::registerUser,
        onLoginClick = onLoginClick,
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun RegistrationScreen(
    showLoadingAnimation: Boolean = false,
    onInputChanged: ()->Unit= {},
    onCreateAccountClick: (String, String, String)->Unit = { _, _, _ -> },
    onLoginClick: ()->Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {

    var email by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var password by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

        AuthScreen(
            analyticsScreen = AnalyticsScreen.Registration,
            title = stringResource(AR.strings.reg_screen_title.resourceId),
            footerText = stringResource(AR.strings.reg_existing_account_prompt.resourceId),
            footerActionText = stringResource(AR.strings.reg_login_button.resourceId),
            onFooterActionClick = onLoginClick,
            snackbarHostState = snackbarHostState
        ) {
            OutlinedTextField(
                label = { Text(stringResource(AR.strings.field_label_email.resourceId)) },
                value = email,
                onValueChange = { value ->
                    email = value
                    onInputChanged()
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
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
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                showPasswordRules = true,
                modifier = Modifier.fillMaxWidth()
            )
            InputSpacer()

            OutlinedTextField(
                label = { Text(stringResource(AR.strings.field_label_name.resourceId)) },
                value = name,
                onValueChange = { value ->
                    name = value
                    onInputChanged()
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            InputSpacer()

            LoadingButton(
                text = stringResource(AR.strings.reg_submit_button.resourceId),
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onCreateAccountClick(email.text, password.text, name.text)
                },
                modifier = Modifier.align(Alignment.End),
                enabled = email.text.isNotBlank() && password.text.isNotBlank() && name.text.isNotBlank(),
                isLoading = showLoadingAnimation
            )
        }
}

@Composable
@Preview
fun RegistrationScreenPreview() {
    MyApplicationTheme {
        RegistrationScreen()
    }
}