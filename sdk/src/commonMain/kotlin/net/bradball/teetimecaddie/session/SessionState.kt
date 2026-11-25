package net.bradball.teetimecaddie.session

import net.bradball.teetimecaddie.core.models.Player

/**
 * The current user's session: whether anyone is signed in, and whether they have a player profile.
 *
 * Decide what the user can do from this, never from whether Firebase has a signed-in user. The two
 * differ in [ProfileIncomplete].
 */
sealed class SessionState {

    /**
     * A persisted session that Firebase has not finished restoring.
     *
     * Only ever the initial value of `SessionManager.sessionState`, when Firebase has a persisted
     * user; the state never returns to it. It lasts until the restored user's profile has been
     * read, so a profile that can't be read yet, offline with nothing cached, is never reported as
     * missing.
     */
    data object Loading : SessionState()

    /** No signed-in user. */
    data object SignedOut : SessionState()

    /**
     * A signed-in user with no player profile: their account was created, but sign-up was never
     * completed.
     *
     * @property userId The signed-in user's id.
     * @property email The signed-in user's email address.
     */
    data class ProfileIncomplete(val userId: String, val email: String) : SessionState()

    /**
     * A signed-in user with a player profile.
     *
     * @property player The signed-in user's player profile.
     */
    data class SignedIn(val player: Player) : SessionState()
}
