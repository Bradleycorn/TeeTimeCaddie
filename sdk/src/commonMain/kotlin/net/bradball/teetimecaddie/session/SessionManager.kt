package net.bradball.teetimecaddie.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.core.models.map
import net.bradball.teetimecaddie.features.auth.AuthErrors
import net.bradball.teetimecaddie.features.auth.AuthException
import net.bradball.teetimecaddie.features.auth.AuthRepository
import net.bradball.teetimecaddie.features.players.PlayerRepository
import net.bradball.teetimecaddie.core.models.TtcLookup

/**
 * Joins authentication to player profiles.
 *
 * The only place that knows about both, which is why it lives in the `:sdk` umbrella — the one
 * module that already depends on each. Without it the join would have to happen in each app, which
 * is the same business logic written twice.
 *
 * Also owns the two-phase sign-up, because neither half can run it alone: [startSignUp] creates the
 * account (auth) and [completeSignUp] saves the profile (players). [completeSignUp] is also why a
 * single [TtcResult] error type earns its keep — it can fail for an authentication reason *or* a
 * profile reason.
 *
 * **Nothing here throws.**
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
     * Succeeds with [SessionState.SignedIn] normally, or [SessionState.ProfileIncomplete] for
     * someone whose sign-up was interrupted before their profile was saved.
     */
    suspend fun signIn(email: String, password: String): TtcResult<SessionState> {
        val user = when (val result = authRepository.signIn(email, password)) {
            is TtcResult.Failure -> return result
            is TtcResult.Success -> result.data
        }

        // No profile means the sign-up never finished: resume the profile step. A read that
        // *failed* is reported instead — routing to the profile step on a network blip would
        // invite the person to re-enter details over a profile that already exists.
        return when (val profile = playerRepository.getPlayer(user.id)) {
            is TtcLookup.Failure -> TtcResult.Failure(profile.error)
            is TtcLookup.Success -> {
                val player = profile.data
                TtcResult.Success(
                    if (player != null) {
                        SessionState.SignedIn(player)
                    } else {
                        SessionState.ProfileIncomplete(user.id, user.email)
                    }
                )
            }
        }
    }

    /**
     * Step one of sign-up: create the account.
     *
     * Rejects an address that is already in use, which is what lets the credentials screen show
     * that before asking for a name and phone number.
     */
    suspend fun startSignUp(email: String, password: String): TtcResult<SessionState.ProfileIncomplete> =
        authRepository.createAccount(email, password)
            .map { SessionState.ProfileIncomplete(it.id, it.email) }

    /**
     * Step two of sign-up: save the profile.
     *
     * @param photo JPEG bytes for the avatar, already downscaled by the caller, or null.
     */
    suspend fun completeSignUp(name: String, phone: String, photo: ByteArray? = null): TtcResult<Player> {
        val user = authRepository.currentUser
            ?: return TtcResult.Failure(AuthException(AuthErrors.SESSION_EXPIRED))

        val player = when (
            val result = playerRepository.createPlayer(
                playerId = user.id,
                name = name,
                email = user.email,
                phone = phone,
                photo = photo
            )
        ) {
            is TtcResult.Failure -> return result
            is TtcResult.Success -> result.data
        }

        // Best-effort, and already non-throwing: the Firebase Auth record's display name is a
        // convenience, not a source of truth, so it must not fail a sign-up already saved.
        authRepository.updateDisplayName(player.name)

        // The user id was already set when the account was created, so the profile step's own
        // analytics are attributed. Only the completion event waits for the profile to exist.
        eventManager.logEvent(AnalyticsEvent.CreateAccount)

        return TtcResult.Success(player)
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
        if (user != null) {
            // Delete only on a successful read that found nothing — positive evidence there is no
            // profile. A read that merely *failed* is not proof of absence, and acting on it would
            // delete a real account because the network blipped.
            val profile = playerRepository.getPlayer(user.id)
            if (profile is TtcLookup.Success && profile.data == null) {
                authRepository.deleteCurrentUser()
                eventManager.logEvent(AnalyticsEvent.AbandonedRegistration(reason))
            }
        }
        authRepository.signOut()
    }

    suspend fun signOut() = authRepository.signOut()

    /** Re-validate the session, e.g. when the app returns to the foreground. */
    suspend fun refreshSession() = authRepository.refreshAuthentication()
}
