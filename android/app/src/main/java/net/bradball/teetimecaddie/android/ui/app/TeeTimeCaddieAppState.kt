package net.bradball.teetimecaddie.android.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import net.bradball.teetimecaddie.android.initializers.AppInitializers
import net.bradball.teetimecaddie.android.initializers.InitializationState
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessage
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.session.SessionManager
import net.bradball.teetimecaddie.session.SessionState

@Composable
fun rememberTeeTimeCaddieAppState(
    appInitializers: AppInitializers,
    sessionManager: SessionManager,
    messenger: TtcMessenger,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): TeeTimeCaddieAppState {
    return remember(coroutineScope, appInitializers, sessionManager, messenger) {
        TeeTimeCaddieAppState(coroutineScope, appInitializers, sessionManager, messenger)
    }
}

class TeeTimeCaddieAppState(
    coroutineScope: CoroutineScope,
    appInitializers: AppInitializers,
    sessionManager: SessionManager,
    messenger: TtcMessenger,
) {
    /**
     * What the app shows: the auth flow, the tabs, or neither yet.
     *
     * The SDK's own shared flow, as is: its synchronous initial value is already the right first
     * frame, so there is nothing to seed or convert here.
     */
    val sessionState: StateFlow<SessionState> = sessionManager.sessionState

    /** Brief confirmations to show, hosted above the auth/tabs branch. See [TtcMessenger]. */
    val messages: Flow<TtcMessage> = messenger.messages

    val appInitStatus = appInitializers.state
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InitializationState.Pending
        )
}
