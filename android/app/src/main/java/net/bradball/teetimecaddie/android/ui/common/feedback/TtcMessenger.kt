package net.bradball.teetimecaddie.android.ui.common.feedback

import dev.icerock.moko.resources.StringResource
import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
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
 * An app-scoped channel for brief messages — a failure in its own words, a warning that the
 * picked photo could not be read, and the confirmations that follow a session transition
 * ("Welcome back, Dana", "Signed out").
 *
 * Those confirmations are why this is app-scoped. The screen that signs someone in is destroyed by
 * the very swap that success triggers, so it cannot show them; `TeeTimeCaddieActivityViewModel`
 * turns the SDK's `SessionEvent`s into messages here instead. The host sits above the auth/tabs
 * branch in `TeeTimeCaddieApp` so it survives that swap too.
 *
 * A buffered [Channel] rather than a `SharedFlow`, because there is exactly one consumer — the root
 * snackbar host — and it is briefly *absent*: rotation recreates the activity, and with it the
 * collector. A `SharedFlow` drops whatever is emitted in that gap; a channel holds it until the new
 * collector arrives, and still delivers each message once. [trySend] means emitting never suspends
 * or blocks a ViewModel that is about to go away.
 */
@Singleton
class TtcMessenger @Inject constructor() {

    private val _messages = Channel<TtcMessage>(Channel.BUFFERED)

    /**
     * The messages to show, each delivered once.
     *
     * Exposed through [receiveAsFlow], never as the channel itself: this is an app-lifetime
     * singleton, and a consumer holding the `ReceiveChannel` could cancel it — `consumeEach` and
     * `consumeAsFlow` both do when their collector stops, which rotation does every time — and
     * silence every message for the rest of the process. A collector cancelled here leaves the
     * channel open for the next one.
     */
    val messages: Flow<TtcMessage> = _messages.receiveAsFlow()

    fun show(message: TtcMessage) {
        _messages.trySend(message)
    }

    fun show(text: StringResource, vararg args: String) {
        show(TtcMessage(text, args.toList()))
    }

    /**
     * Shows a failure in its own words.
     *
     * The catch-all for failures no screen draws a designed treatment for. Without it, mapping
     * "the cases the design covers" would silently swallow every other one — a weak password or a
     * dropped connection would look like the button simply did nothing.
     */
    fun show(error: TeeTimeCaddieException) {
        show(TtcMessage(error.displayMessage, error.messageArgs))
    }
}
