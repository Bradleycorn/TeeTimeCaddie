package net.bradball.teetimecaddie.session

import net.bradball.teetimecaddie.core.models.Player

/**
 * A session transition the person caused, reported once, for one-off feedback such as "Welcome
 * back, Dana".
 *
 * Not the session itself — that is [SessionState], which apps route on. These exist because the
 * screen that causes a transition is destroyed by it (the root swaps the whole view tree), so it
 * cannot show the confirmation itself. The app root, which hosts the toast and survives the swap,
 * listens for these instead.
 *
 * Only transitions someone *asked for* are reported: a session restored on launch, a sign-up that
 * was abandoned, or an expired token emit nothing.
 */
sealed class SessionEvent {

    /** An existing, fully provisioned account signed in. */
    data class SignedIn(val player: Player) : SessionEvent()

    /** Sign-up finished: the profile was saved, and the person is now signed in. */
    data class AccountCreated(val player: Player) : SessionEvent()

    /** The person signed out. */
    data object SignedOut : SessionEvent()
}
