package net.bradball.teetimecaddie.session

import net.bradball.teetimecaddie.core.models.Player

/**
 * What the app knows about the person using it, right now.
 *
 * Both apps branch their whole UI on this, so it is the integration contract between the SDK and
 * the platform layers: **route on `SessionState`, never on "is there a Firebase user"**. Those two
 * are not the same thing, and [ProfileIncomplete] is exactly where they differ.
 */
sealed class SessionState {

    /**
     * Firebase has not finished restoring a persisted session.
     *
     * Only ever the seed value, and only when a session probably exists. Without it, a cold start
     * shows the credentials screen for a frame before the restored user arrives — which reads as a
     * flash of "signed out" to someone who never signed out.
     */
    data object Loading : SessionState()

    /** No account. The app shows the credentials screen. */
    data object SignedOut : SessionState()

    /**
     * An authenticated user with no profile yet.
     *
     * This is the cost of rejecting an in-use email *before* the profile step: the Firebase
     * account is created from the credentials screen, so between that and saving a profile there
     * is a real, reachable state with a session and no player. Modelling it means someone who
     * force-quits mid-sign-up resumes at the profile step instead of landing on an empty Games tab.
     */
    data class ProfileIncomplete(val userId: String, val email: String) : SessionState()

    /** Fully provisioned. The app shows the Games tab, and the Profile tab renders [player]. */
    data class SignedIn(val player: Player) : SessionState()
}
