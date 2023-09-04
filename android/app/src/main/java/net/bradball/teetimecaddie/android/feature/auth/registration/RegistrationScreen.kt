package net.bradball.teetimecaddie.android.feature.auth.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.forms.InputSpacer
import net.bradball.teetimecaddie.features.auth.AR

private const val screenName = "Registration"

@Composable
fun RegistrationRoute(
    onLoginClick: () -> Unit,
    onRegistrationComplete: ()->Unit,
    onShowSnackbar: suspend (String, String?)-> SnackbarResult,
    viewModel: RegistrationViewModel = hiltViewModel()) {

    val context = LocalContext.current
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { messageId ->
           onShowSnackbar(context.getString(messageId), null)
           viewModel.clearError()
        }
    }

    RegistrationScreen(
        showButtonSpinner = viewModel.showButtonSpinner,
        onInputChanged = viewModel::clearError,
        onCreateAccountClick = { email, password, name ->
            viewModel.registerUser(email, password, name, onRegistrationComplete)
        },
        onLoginClick = onLoginClick)
}

@Composable
private fun RegistrationScreen(
    showButtonSpinner: Boolean = false,
    onInputChanged: ()->Unit= {},
    onCreateAccountClick: (String, String, String)->Unit = { _, _, _ -> },
    onLoginClick: ()->Unit = {}
) {

    var email by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var password by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var passwordVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val passwordTransformation = when {
        passwordVisible -> VisualTransformation.None
        else -> PasswordVisualTransformation()
    }

    val passwordIcon = when {
        passwordVisible -> Icons.Outlined.VisibilityOff
        else -> Icons.Outlined.Visibility
    }

    Screen(screenName) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(id = AR.strings.reg_screen_title.resourceId)) },
                )
                Icon(
                    Icons.Filled.SportsGolf,
                    contentDescription = "TeeTimeCaddie",
                    modifier = Modifier.size(128.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(32.dp))

                OutlinedTextField(
                    label = { Text("Email Address") },
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

                OutlinedTextField(
                    label = { Text("Password") },
                    value = password,
                    onValueChange = { value ->
                        password = value
                        onInputChanged()
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = passwordIcon,
                                contentDescription = "Toggle Password Visibility"
                            )
                        }
                    },
                    visualTransformation = passwordTransformation,
                    supportingText = { Text("At least 8 characters. At least 1 number.") },
                    modifier = Modifier.fillMaxWidth()
                )
                InputSpacer()

                OutlinedTextField(
                    label = { Text("Name") },
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.align(Alignment.End)
                ) {

                    if (showButtonSpinner) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    Button(
                        enabled = !showButtonSpinner,
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onCreateAccountClick(email.text, password.text, name.text)
                        }
                    ) {

                        Text(stringResource(AR.strings.reg_submit_button.resourceId))
                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                LoginLink(onLoginClick = onLoginClick)
                InputSpacer()
            }
        }
    }
}



@Composable
private fun LoginLink(onLoginClick: ()->Unit = {}) {
    Row {
        Text(stringResource(AR.strings.reg_existing_account_prompt.resourceId), modifier = Modifier.alignByBaseline())
        TextButton(onClick = { onLoginClick() }, modifier = Modifier.alignByBaseline()) {
            Text(stringResource(id = AR.strings.reg_login_button.resourceId))
        }
    }
}


@Composable
@Preview
fun RegistrationScreenPreview() {
    MyApplicationTheme {
        RegistrationScreen()
    }
}