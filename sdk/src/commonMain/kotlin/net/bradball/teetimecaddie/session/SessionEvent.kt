package net.bradball.teetimecaddie.session

import net.bradball.teetimecaddie.core.models.Player

/**
 * A session change the user asked for, reported once by `SessionManager.sessionEvents`.
 *
 * An event reports a change, not the session itself; for that, observe [SessionState]. Restoring a
 * session at launch, abandoning a sign-up, and an expired token report nothing.
 */
sealed class SessionEvent {

    /**
     * A sign-in to an account that has a player profile.
     *
     * @property player The signed-in user's player profile.
     */
    data class SignedIn(val player: Player) : SessionEvent()

    /**
     * A completed sign-up: the player profile was saved and the user is signed in.
     *
     * @property player The new player profile.
     */
    data class AccountCreated(val player: Player) : SessionEvent()

    /** A sign-out the user requested. */
    data object SignedOut : SessionEvent()
}
