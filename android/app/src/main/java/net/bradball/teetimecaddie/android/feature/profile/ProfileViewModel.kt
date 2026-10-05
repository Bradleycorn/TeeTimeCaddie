package net.bradball.teetimecaddie.android.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
     *
     * [NonCancellable] because that swap is what destroys this ViewModel: the moment `signOut()`
     * returns, `SessionState` goes `SignedOut`, the tab tree is replaced, and `viewModelScope` is
     * cancelled — while this coroutine is still suspended. The resumption, and with it the
     * confirmation, was being dropped every time. Routing through the messenger is not enough on
     * its own; the emission has to survive the cancellation to reach it.
     *
     * An application-scoped `CoroutineScope` would also solve this, but injecting one into a
     * ViewModel is the thing we deliberately avoided earlier in this story — that is why
     * `SessionManager` owns the scope for `abandonSignUp`. Sign-out messaging cannot move with it,
     * because string resources and the messenger are app-layer.
     */
    fun signOut() {
        viewModelScope.launch {
            withContext(NonCancellable) {
                sessionManager.signOut()
                // Through the messenger, not local state: signing out replaces the whole tab tree,
                // so this ViewModel is gone before anything here could show a confirmation.
                messenger.show(AR.strings.auth_toast_signed_out)
            }
        }
    }

    private fun SessionState.toUiState(): ProfileUiState = when (this) {
        is SessionState.SignedIn -> ProfileUiState.Content(player)
        else -> ProfileUiState.Loading
    }
}
