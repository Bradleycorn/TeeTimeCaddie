package net.bradball.teetimecaddie.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.features.auth.AuthErrors
import net.bradball.teetimecaddie.features.auth.AuthException
import net.bradball.teetimecaddie.features.auth.AuthRepository
import net.bradball.teetimecaddie.features.players.PlayerRepository
import kotlin.coroutines.cancellation.CancellationException

/**
 * Joins authentication to player profiles.
 *
 * The only place that knows about both, which is why it lives in the `:sdk` umbrella — the one
 * module that already depends on each. Without it the join would have to happen in each app, which
 * is the same business logic written twice.
 *
 * Also owns the two-phase sign-up, because neither half can run it alone: [startSignUp] creates the
 * account (auth) and [completeSignUp] saves the profile (players).
 */
class SessionManager(
    private val authRepository: AuthRepository,
    private val playerRepository: PlayerRepository,
    private val eventManager: EventManager
) {

    /**
     * A synchronous first value for [sessionState], so a cold start never flashes the credentials
     * screen at someone who is signed in. Apps should use this as the initial value when they
     * convert [sessionState] into their own observable state.
     */
    val initialSessionState: SessionState
        get() = if (authRepository.currentUser != null) SessionState.Loading else SessionState.SignedOut

    /**
     * The current session, re-emitting on sign-in, sign-out, and any change to the signed-in
     * player's profile — which is what makes a freshly uploaded avatar appear without a refresh.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val sessionState: Flow<SessionState>
        get() = authRepository.authChanges.flatMapLatest { user ->
            if (user == null) {
                flowOf(SessionState.SignedOut)
            } else {
                playerRepository.playerFlow(user.id).map { player ->
                    player?.let { SessionState.SignedIn(it) }
                        ?: SessionState.ProfileIncomplete(user.id, user.email)
                }
            }
        }

    /**
     * Sign in.
     *
     * @return [SessionState.SignedIn] normally, or [SessionState.ProfileIncomplete] for someone
     *   whose sign-up was interrupted before their profile was saved.
     */
    @Throws(AuthException::class, CancellationException::class)
    suspend fun signIn(email: String, password: String): SessionState {
        val user = authRepository.signIn(email, password)
        val player = playerRepository.getPlayer(user.id)
        return player?.let { SessionState.SignedIn(it) }
            ?: SessionState.ProfileIncomplete(user.id, user.email)
    }

    /**
     * Step one of sign-up: create the account.
     *
     * Rejects an address that is already in use, which is what lets the credentials screen show
     * that before asking for a name and phone number.
     */
    @Throws(AuthException::class, CancellationException::class)
    suspend fun startSignUp(email: String, password: String): SessionState.ProfileIncomplete {
        val user = authRepository.createAccount(email, password)
        return SessionState.ProfileIncomplete(user.id, user.email)
    }

    /**
     * Step two of sign-up: save the profile.
     *
     * @param photo JPEG bytes for the avatar, already downscaled by the caller, or null.
     * @throws AuthException [AuthErrors.SESSION_EXPIRED] if the account vanished between the steps.
     */
    @Throws(Exception::class, CancellationException::class)
    suspend fun completeSignUp(name: String, phone: String, photo: ByteArray? = null): Player {
        val user = authRepository.currentUser ?: throw AuthException(AuthErrors.SESSION_EXPIRED)

        val player = playerRepository.createPlayer(
            playerId = user.id,
            name = name,
            email = user.email,
            phone = phone,
            photo = photo
        )

        // Best-effort: the Firebase Auth record's display name is a convenience, not a source of
        // truth, so failing to update it must not fail a sign-up that has already been saved.
        runCatching { authRepository.updateDisplayName(player.name) }

        // Only now is the account real, so only now is it counted.
        eventManager.setUserId(user.id)
        eventManager.logEvent(AnalyticsEvent.CreateAccount)

        return player
    }

    /**
     * Abandon a sign-up that never got a profile, deleting the half-made account.
     *
     * Guarded on the profile's absence, so this can be called from anywhere — a back press, a
     * "Sign in instead" tap — and can never delete a provisioned account. Deletion is best-effort:
     * Firebase refuses it for a session old enough to need re-authentication, and an orphaned auth
     * user is harmless because the app resumes at [SessionState.ProfileIncomplete] anyway.
     */
    suspend fun abandonSignUp(reason: String? = null) {
        val user = authRepository.currentUser
        if (user != null && playerRepository.getPlayer(user.id) == null) {
            runCatching { authRepository.deleteCurrentUser() }
            eventManager.logEvent(AnalyticsEvent.AbandonedRegistration(reason))
        }
        authRepository.signOut()
    }

    suspend fun signOut() = authRepository.signOut()

    /** Re-validate the session, e.g. when the app returns to the foreground. */
    suspend fun refreshSession() = authRepository.refreshAuthentication()
}
