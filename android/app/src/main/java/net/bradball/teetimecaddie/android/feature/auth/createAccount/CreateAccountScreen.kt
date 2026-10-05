package net.bradball.teetimecaddie.android.feature.auth.createAccount

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bradball.teetimecaddie.android.feature.auth.common.AuthMessage
import net.bradball.teetimecaddie.android.feature.auth.common.AuthMessageBlock
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.theme.TtcColorRole
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.appBars.TtcTopAppBar
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcPhotoPicker
import net.bradball.teetimecaddie.android.ui.common.avatars.rememberTtcImagePainter
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcButton
import net.bradball.teetimecaddie.android.ui.common.forms.TtcTextField
import net.bradball.teetimecaddie.android.ui.common.forms.TtcTextFieldDefaults
import net.bradball.teetimecaddie.android.ui.common.forms.filters.PhoneVisualTransformation
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.preview.ScreenPreviews
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.features.auth.AR
import net.bradball.teetimecaddie.features.players.PR

/**
 * The profile step of creating an account: name, mobile number and an optional photo.
 *
 * Reached only once the Firebase account exists, so leaving without finishing has to clean up after
 * itself — both the app bar's back arrow and the system back gesture abandon the sign-up.
 *
 * @param viewModel Built by the navigation entry with the email from the nav key.
 * @param onBack Returns to the credentials screen.
 */
@Composable
fun CreateAccountScreen(
    viewModel: CreateAccountViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
        viewModel::onPhotoPicked,
    )

    // Both ways out run the same cleanup. Nothing else may leave this screen: every other exit is a
    // SessionState change, which replaces the whole tree.
    val leave = {
        viewModel.abandonSignUp()
        onBack()
    }

    BackHandler(onBack = leave)

    Screen(AnalyticsScreen.CreateAccount("CreateAccountScreen")) {
        CreateAccountContent(
            uiState = uiState,
            photoPainter = rememberTtcImagePainter(uiState.photo),
            onNameChange = viewModel::onNameChange,
            onPhoneChange = viewModel::onPhoneChange,
            onPickPhoto = {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemovePhoto = viewModel::removePhoto,
            onSubmit = viewModel::submit,
            onMessageAction = viewModel::onMessageAction,
            onBack = leave,
        )
    }
}

/**
 * @param photoPainter The picked photo, once it has loaded. Distinct from
 *   [CreateAccountUiState.photo], which is the `Uri` that was picked: the two differ while the
 *   image is still loading, which is why the picker draws this and the caption reads the state.
 *   Loading happens in the caller so this stays previewable without a network loader.
 */
@Composable
private fun CreateAccountContent(
    uiState: CreateAccountUiState,
    photoPainter: Painter?,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onSubmit: () -> Unit,
    onMessageAction: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val nameFocus = remember { FocusRequester() }
    val phoneFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) { nameFocus.requestFocus() }

    Scaffold(
        modifier = modifier.imePadding(),
        topBar = {
            TtcTopAppBar(
                title = stringResource(AR.strings.create_account_title.resourceId),
                onBack = onBack,
            )
        },
        bottomBar = {
            // In the bottom bar rather than in the scrolling column, so the CTA stays reachable
            // above the keyboard instead of being something you have to scroll down to find.
            TtcButton(
                text = stringResource(AR.strings.create_account_title.resourceId),
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(CreateAccountDefaults.Padding),
                color = TtcColorRole.Primary,
                enabled = uiState.canSubmit,
                isLoading = uiState.isSubmitting,
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = CreateAccountDefaults.Padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CreateAccountDefaults.Spacing),
        ) {
            Text(
                text = stringResource(AR.strings.create_account_heading.resourceId),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(AR.strings.create_account_subheading.resourceId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            // Tapping a disc that already holds a photo removes it; only the empty disc opens the
            // picker. The branch lives here rather than in TtcPhotoPicker, which stays
            // presentational, and it mirrors CreateAccountScreen.swift branching on `hasPhoto`.
            TtcPhotoPicker(
                onClick = if (uiState.photo == null) onPickPhoto else onRemovePhoto,
                photo = photoPainter,
            )

            Text(
                text = stringResource(
                    if (uiState.photo == null) {
                        PR.strings.photo_caption_empty.resourceId
                    } else {
                        PR.strings.photo_caption_added.resourceId
                    }
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            uiState.message?.let { message ->
                AuthMessageBlock(message = message, onAction = onMessageAction)
            }

            TtcTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = stringResource(PR.strings.field_label_full_name.resourceId),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nameFocus),
                leadingIcon = TtcIcons.PERSON_OUTLINE,
                enabled = !uiState.isSubmitting,
                keyboardOptions = TtcTextFieldDefaults.NameKeyboardOptions
                    .copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { phoneFocus.requestFocus() }),
            )

            TtcTextField(
                // Raw digits in, grouping applied only on the way to the screen. The ViewModel
                // never sees "(502) 555-1234", and the caret stays indexed against the digits.
                value = uiState.phoneDigits,
                onValueChange = onPhoneChange,
                visualTransformation = PhoneVisualTransformation,
                label = stringResource(PR.strings.field_label_mobile.resourceId),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(phoneFocus),
                leadingIcon = TtcIcons.PHONE,
                hint = stringResource(PR.strings.mobile_hint.resourceId),
                error = uiState.phoneError?.let { stringResource(it.resourceId) },
                enabled = !uiState.isSubmitting,
                keyboardOptions = TtcTextFieldDefaults.PhoneKeyboardOptions
                    .copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboard?.hide()
                        onSubmit()
                    }
                ),
            )
        }
    }
}

private object CreateAccountDefaults {
    val Padding = 24.dp
    val Spacing = 16.dp
}

@ScreenPreviews
@Composable
private fun CreateAccountContentPreview() {
    MyApplicationTheme {
        CreateAccountContent(
            uiState = CreateAccountUiState(email = "dana@example.com"),
            photoPainter = null,
            onNameChange = {}, onPhoneChange = {}, onPickPhoto = {}, onRemovePhoto = {},
            onSubmit = {}, onMessageAction = {}, onBack = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun CreateAccountContentPhotoPreview() {
    MyApplicationTheme {
        CreateAccountContent(
            uiState = CreateAccountUiState(
                email = "dana@example.com",
                name = "Dana Whitfield",
                phoneDigits = "5025551234",
                photo = Uri.EMPTY,
            ),
            photoPainter = ColorPainter(MaterialTheme.colorScheme.tertiaryContainer),
            onNameChange = {}, onPhoneChange = {}, onPickPhoto = {}, onRemovePhoto = {},
            onSubmit = {}, onMessageAction = {}, onBack = {},
        )
    }
}

/** The one case that shows a block and an inline field error at the same time. */
@ScreenPreviews
@Composable
private fun CreateAccountContentPhoneInUsePreview() {
    MyApplicationTheme {
        CreateAccountContent(
            uiState = CreateAccountUiState(
                email = "dana@example.com",
                name = "Dana Whitfield",
                phoneDigits = "5025551234",
                phoneError = PR.strings.player_error_phone_in_use_inline,
                message = AuthMessage.PhoneInUse,
            ),
            photoPainter = null,
            onNameChange = {}, onPhoneChange = {}, onPickPhoto = {}, onRemovePhoto = {},
            onSubmit = {}, onMessageAction = {}, onBack = {},
        )
    }
}
