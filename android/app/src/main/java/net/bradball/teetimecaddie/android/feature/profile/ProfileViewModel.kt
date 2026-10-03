package net.bradball.teetimecaddie.android.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.features.auth.AR
import net.bradball.teetimecaddie.session.SessionManager
import net.bradball.teetimecaddie.session.SessionState
import javax.inject.Inject

/**
 * State for the Profile screen.
 *
 * There is no error case: the player shown here is the one already held in
 * [SessionState.SignedIn], so reaching this screen at all means the read succeeded.
 */
sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Content(val player: Player) : ProfileUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val messenger: TtcMessenger,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = sessionManager.sessionState
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = sessionManager.initialSessionState.toUiState()
        )

    /**
     * Signs the player out.
     *
     * Nothing navigates afterwards — `TeeTimeCaddieApp` swaps the whole tree when `SessionState`
     * becomes `SignedOut`.
     */
    fun signOut() {
        viewModelScope.launch {
            sessionManager.signOut()
            // Through the messenger, not local state: signing out replaces the whole tab tree, so
            // this ViewModel is gone before anything here could show a confirmation.
            messenger.show(AR.strings.auth_toast_signed_out)
        }
    }

    private fun SessionState.toUiState(): ProfileUiState = when (this) {
        is SessionState.SignedIn -> ProfileUiState.Content(player)
        else -> ProfileUiState.Loading
    }
}
