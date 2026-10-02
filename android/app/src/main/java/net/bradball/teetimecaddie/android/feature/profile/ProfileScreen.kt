package net.bradball.teetimecaddie.android.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.theme.TtcColorRole
import net.bradball.teetimecaddie.android.ui.common.ContentLoadingIndicator
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.appBars.TtcCenteredTopAppBar
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcAvatar
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcAvatarDefaults
import net.bradball.teetimecaddie.android.ui.common.avatars.rememberTtcImagePainter
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcOutlinedButton
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.preview.ScreenPreviews
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.core.models.previewPlayer
import net.bradball.teetimecaddie.features.players.PR

/**
 * The Profile tab: who you are, and the way out.
 *
 * Read-only by design. There is deliberately **no settings list and no version line** — editing a
 * profile is its own story, and the design gives this screen nothing else to do.
 */
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val player = (uiState as? ProfileUiState.Content)?.player

    Screen(AnalyticsScreen.Profile("ProfileScreen")) {
        ProfileContent(
            uiState = uiState,
            photoPainter = rememberTtcImagePainter(player?.photoUrl),
            onSignOut = viewModel::signOut,
        )
    }
}

/**
 * @param photoPainter The player's avatar, once it has loaded. Loading happens in the caller so
 *   this stays previewable without a network loader; `null` means fall back to the initial.
 */
@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    photoPainter: Painter?,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TtcCenteredTopAppBar(stringResource(PR.strings.profile_title.resourceId)) },
    ) { contentPadding ->
        when (uiState) {
            ProfileUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                ContentLoadingIndicator()
            }

            is ProfileUiState.Content -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(ProfileDefaults.Padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ProfileDefaults.Spacing),
            ) {
                // The initial is the fallback, not a placeholder: rememberTtcImagePainter returns
                // null until a photo has actually loaded, so a broken URL degrades to the letter
                // rather than to a hole.
                if (photoPainter != null) {
                    TtcAvatar(photo = photoPainter, size = TtcAvatarDefaults.PickerSize)
                } else {
                    TtcAvatar(
                        initials = uiState.player.initial,
                        size = TtcAvatarDefaults.PickerSize,
                    )
                }

                Text(
                    text = uiState.player.name,
                    style = MaterialTheme.typography.headlineSmall,
                )

                ProfileDetailRow(icon = TtcIcons.EMAIL, value = uiState.player.email)
                ProfileDetailRow(icon = TtcIcons.PHONE, value = uiState.player.formattedPhone)

                TtcOutlinedButton(
                    text = stringResource(PR.strings.profile_sign_out_button.resourceId),
                    onClick = onSignOut,
                    modifier = Modifier.padding(top = ProfileDefaults.SignOutSpacing),
                    color = TtcColorRole.Error,
                    icon = TtcIcons.LOGOUT,
                )
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(icon: TtcIcons, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ProfileDefaults.RowSpacing),
    ) {
        Icon(
            painter = icon.painter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(ProfileDefaults.RowIconSize),
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

private object ProfileDefaults {
    val Padding = 24.dp
    val Spacing = 16.dp
    val RowSpacing = 12.dp
    val RowIconSize = 20.dp
    val SignOutSpacing = 16.dp
}

@ScreenPreviews
@Composable
private fun ProfileContentPhotoPreview() {
    MyApplicationTheme {
        ProfileContent(
            uiState = ProfileUiState.Content(previewPlayer),
            photoPainter = ColorPainter(MaterialTheme.colorScheme.tertiaryContainer),
            onSignOut = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun ProfileContentInitialPreview() {
    MyApplicationTheme {
        ProfileContent(
            uiState = ProfileUiState.Content(previewPlayer.copy(photoUrl = null)),
            photoPainter = null,
            onSignOut = {},
        )
    }
}
