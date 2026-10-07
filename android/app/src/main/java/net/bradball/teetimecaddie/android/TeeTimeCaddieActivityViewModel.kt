package net.bradball.teetimecaddie.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.bradball.teetimecaddie.android.initializers.AppInitializers
import net.bradball.teetimecaddie.android.initializers.InitializationState
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.features.auth.AR
import net.bradball.teetimecaddie.session.SessionEvent
import net.bradball.teetimecaddie.session.SessionManager
import javax.inject.Inject

@HiltViewModel
class TeeTimeCaddieActivityViewModel @Inject constructor(
    val appInitializers: AppInitializers,
    val sessionManager: SessionManager,
    val eventManager: EventManager,
    val messenger: TtcMessenger
): ViewModel() {

    /**
     * Confirms each session transition the person caused — "Welcome back, Dana", "Signed out".
     *
     * Here, rather than in the screen that caused it, because that screen is destroyed by the very
     * transition it would be confirming: the root swaps the whole tree when the session changes.
     * This ViewModel sits above that branch and survives rotation, so nothing emitted is missed, and
     * the messenger's host is already at the root to show it.
     */
    init {
        viewModelScope.launch {
            sessionManager.sessionEvents.collect { event ->
                when (event) {
                    is SessionEvent.SignedIn ->
                        messenger.show(AR.strings.auth_toast_welcome_back, event.player.firstName)
                    is SessionEvent.AccountCreated ->
                        messenger.show(AR.strings.auth_toast_account_created, event.player.firstName)
                    is SessionEvent.SignedOut ->
                        messenger.show(AR.strings.auth_toast_signed_out)
                }
            }
        }
    }

    /**
     * Whether or not to show the splash screen.
     * This value will initially be true, and will change to false once all necessary
     * app startup routines have completed (or the app startup failed).
     */
    val showSplashScreen: Flow<Boolean> = appInitializers.state.map { it == InitializationState.Pending }

    /**
     * Whether or not the application can be displayed.
     * This value will initially be false, and will change to true once all necessary
     * app startup routines have completed (or the app startup failed).
     */
    val showApp: StateFlow<Boolean> = appInitializers.initState
        .map { it != InitializationState.Pending }
        .stateIn(
            scope = viewModelScope,
            initialValue = false,
            started = SharingStarted.WhileSubscribed(1_000)
        )
}