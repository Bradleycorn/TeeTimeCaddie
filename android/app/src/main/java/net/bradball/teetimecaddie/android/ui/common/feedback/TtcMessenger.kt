package net.bradball.teetimecaddie.android.ui.common.feedback

import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A short confirmation to show the person, as a string resource plus its format arguments.
 *
 * Resolved at the point of display rather than here, so the SDK and ViewModels never need a
 * `Context`.
 */
data class TtcMessage(
    val text: StringResource,
    val args: List<String> = emptyList(),
)

/**
 * An app-scoped channel for the brief confirmations that follow an auth transition — "Welcome
 * back, Dana", "Signed out".
 *
 * This exists because of *when* those messages are produced. The ViewModel that knows the sign-in
 * succeeded is destroyed by the very navigation that success triggers, so a message emitted from
 * the screen's own scope would be cancelled before anything could show it. Routing through a
 * singleton lets the message outlive the screen that asked for it, and the host sits above the
 * auth/tabs branch in `TeeTimeCaddieApp` so it survives that swap too.
 *
 * Uses a buffered [MutableSharedFlow] with [tryEmit] so emitting never suspends and never blocks a
 * ViewModel that is about to go away.
 */
@Singleton
class TtcMessenger @Inject constructor() {

    private val _messages = MutableSharedFlow<TtcMessage>(extraBufferCapacity = 4)

    val messages: SharedFlow<TtcMessage> = _messages.asSharedFlow()

    fun show(message: TtcMessage) {
        _messages.tryEmit(message)
    }

    fun show(text: StringResource, vararg args: String) {
        show(TtcMessage(text, args.toList()))
    }
}
