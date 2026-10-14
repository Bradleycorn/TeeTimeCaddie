package net.bradball.teetimecaddie.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
 *
 * **Session-changing work runs in [externalScope], never in the caller's.** Both apps swap their
 * whole view tree when [sessionState] changes, so the screen that signs someone in, finishes their
 * sign-up, or signs them out is destroyed by its own success — usually while the call is still
 * running, because Firestore and Firebase Auth report the change before the call returns. Work run
 * in that screen's scope would be cancelled part-way through. Which steps must survive is a property
 * of the work, not of whoever triggers it, so this class guarantees it rather than leaving every
 * screen on every platform to rediscover it.
 *
 * The constructor is `internal`: apps get the one instance from `TeeTimeCaddieSdk.sessionManager`,
 * and constructing a second would mean a second [sessionState] and [sessionEvents] for an app shell
 * to observe the wrong one of. It also keeps [CoroutineScope] out of the exported Swift API, where
 * it is noise nothing on that side can use.
 *
 * @param externalScope A scope that outlives any screen. Hosts the shared [sessionState], and every
 *   call that changes the session: the `suspend` ones await their work there (see [nonCancelling])
 *   and [abandonSignUp] and [signOut] launch into it.
 */
class SessionManager internal constructor(
    private val authRepository: AuthRepository,
    private val playerRepository: PlayerRepository,
    private val eventManager: EventManager,
    private val externalScope: CoroutineScope
) {

    /**
     * The current session, re-emitting on sign-in, sign-out, and any change to the signed-in
     * player's profile — which is what makes a freshly uploaded avatar appear without a refresh.
     *
     * One flow, shared by every observer, so the app root and any screen that reads the player
     * share a single auth listener and a single Firestore listener. Started on the first
     * subscriber, and kept running after that: the session matters for the life of the process.
     *
     * Its initial value is synchronous, so a cold start never flashes the credentials screen at
     * someone who is signed in: [SessionState.Loading] when Firebase has a persisted user still to
     * resolve, [SessionState.SignedOut] when it has none. Apps can read [StateFlow.value] for a
     * first render instead of inventing their own seed.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val sessionState: StateFlow<SessionState> = authRepository.authChanges
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(SessionState.SignedOut)
            } else {
                playerRepository.playerFlow(user.id).map { player ->
                    player?.let { SessionState.SignedIn(it) }
                        ?: SessionState.ProfileIncomplete(user.id, user.email)
                }
            }
        }
        .stateIn(
            scope = externalScope,
            started = SharingStarted.Lazily,
            initialValue = if (authRepository.currentUser != null) SessionState.Loading else SessionState.SignedOut
        )

    private val _sessionEvents = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 4)

    /**
     * Transitions the person caused — signing in, finishing sign-up, signing out — for the app root
     * to confirm. See [SessionEvent].
     *
     * Not the session: route on [sessionState]. There is no replay, so a collector only hears what
     * is emitted while it is subscribed; the app root subscribes for the life of the app. Emitted
     * with `tryEmit` into a buffer, so reporting never holds up the work it reports on.
     */
    val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents.asSharedFlow()

    /**
     * Sign in.
     *
     * Succeeds with [SessionState.SignedIn] normally, or [SessionState.ProfileIncomplete] for
     * someone whose sign-up was interrupted before their profile was saved. Only the first is
     * reported on [sessionEvents].
     */
    suspend fun signIn(email: String, password: String): TtcResult<SessionState> =
        nonCancelling {
            performSignIn(email, password).also { result ->
                val state = (result as? TtcResult.Success)?.data
                if (state is SessionState.SignedIn) _sessionEvents.tryEmit(SessionEvent.SignedIn(state.player))
            }
        }

    private suspend fun performSignIn(email: String, password: String): TtcResult<SessionState> {
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
        nonCancelling {
            authRepository.createAccount(email, password)
                .map { SessionState.ProfileIncomplete(it.id, it.email) }
        }

    /**
     * Step two of sign-up: save the profile.
     *
     * The clearest case for [nonCancelling]: saving the profile flips [sessionState] to
     * [SessionState.SignedIn] the moment the document is written, which destroys the calling screen
     * while the display-name sync and the `CreateAccount` event are still to run.
     *
     * @param photo JPEG bytes for the avatar, already downscaled by the caller, or null.
     */
    suspend fun completeSignUp(name: String, phone: String, photo: ByteArray? = null): TtcResult<Player> =
        nonCancelling {
            performCompleteSignUp(name, phone, photo).also { result ->
                if (result is TtcResult.Success) _sessionEvents.tryEmit(SessionEvent.AccountCreated(result.data))
            }
        }

    private suspend fun performCompleteSignUp(name: String, phone: String, photo: ByteArray?): TtcResult<Player> {
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
     * Safe to call from anywhere, at any time — a back press, a "Sign in instead" tap, a view
     * disappearing as the tree swaps after sign-up *succeeded*. A provisioned account is left
     * entirely alone: not deleted, and not signed out. Otherwise the person is signed out, and the
     * account is deleted only on positive evidence that it has no profile.
     *
     * Deletion is best-effort: Firebase refuses it for a session old enough to need
     * re-authentication, and an orphaned auth user is harmless because the app resumes at
     * [SessionState.ProfileIncomplete] anyway.
     *
     * Not `suspend`, and launched on [externalScope]: every caller abandons sign-up by *leaving*,
     * and that same act destroys whatever was waiting on the call. Nothing is reported back, and
     * nothing is emitted on [sessionEvents] — callers have already moved on, and [sessionState]
     * tells the app when it lands.
     */
    fun abandonSignUp(reason: String? = null) {
        externalScope.launch {
            val user = authRepository.currentUser
            if (user != null) {
                when (val profile = playerRepository.getPlayer(user.id)) {
                    is TtcLookup.Success -> {
                        // Already provisioned: this is a screen being torn down after sign-up
                        // finished, not someone abandoning it.
                        if (profile.data != null) return@launch

                        authRepository.deleteCurrentUser()
                        eventManager.logEvent(AnalyticsEvent.AbandonedRegistration(reason))
                    }
                    // A read that merely *failed* is not proof of absence, and deleting on it
                    // would destroy a real account because the network blipped. Still sign out:
                    // the person asked to leave.
                    is TtcLookup.Failure -> Unit
                }
            }
            authRepository.signOut()
        }
    }

    /**
     * Sign out.
     *
     * Not `suspend`, and launched on [externalScope], because signing out is the one call here
     * whose caller is not waiting on an answer: it returns nothing, it cannot fail in a way anyone
     * acts on, and the UI reacts to [sessionState] rather than to this returning.
     *
     * Making it `suspend` was a trap. The caller is invariably a screen that signing out destroys —
     * clearing the session swaps the whole view tree — so the awaiting coroutine was cancelled
     * mid-flight and anything sequenced after it silently never ran. Owning the scope here means
     * no caller has to know that. The confirmation is reported on [sessionEvents] for the same
     * reason: the screen that asked is gone before it could show one.
     */
    fun signOut() {
        externalScope.launch {
            authRepository.signOut()
            _sessionEvents.tryEmit(SessionEvent.SignedOut)
        }
    }

    /** Re-validate the session, e.g. when the app returns to the foreground. */
    suspend fun refreshSession() = authRepository.refreshAuthentication()

    /**
     * Run session-changing work in [externalScope] and await its result.
     *
     * If the caller is cancelled — typically because the work's own effect on [sessionState]
     * swapped the view tree out from under it — only the *wait* is abandoned. The work runs to
     * completion, so a sign-up that has written its profile still syncs the display name and logs
     * its event.
     */
    private suspend fun <T> nonCancelling(block: suspend () -> T): T =
        externalScope.async { block() }.await()
}
