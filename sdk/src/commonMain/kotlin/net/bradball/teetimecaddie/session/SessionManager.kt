package net.bradball.teetimecaddie.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
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
 * Tracks the current user's session, joining their Firebase Auth account to their player profile,
 * and makes the calls that change it: signing in and out, and the two-step sign-up of
 * [startSignUp] then [completeSignUp].
 *
 * Nothing here throws; failures come back as [TtcResult.Failure]. Every call that changes the
 * session runs to completion even when the caller's scope is cancelled, including callers whose
 * scope is cancelled by the state change itself. Use the single instance from
 * `TeeTimeCaddieSdk.sessionManager`.
 *
 * @param authRepository The source of the signed-in Firebase Auth user.
 * @param playerRepository The source of player profiles.
 * @param eventManager Where analytics events are logged.
 * @param externalScope A scope that outlives every caller. Hosts [sessionState], and runs every call
 *   that changes the session: the `suspend` ones await their work there (see [nonCancelling]), and
 *   [abandonSignUp], [signOut] and [refreshSession] launch into it.
 */
class SessionManager internal constructor(
    private val authRepository: AuthRepository,
    private val playerRepository: PlayerRepository,
    private val eventManager: EventManager,
    private val externalScope: CoroutineScope
) {

    /**
     * The current session, re-emitted on sign-in, sign-out, and every change to the signed-in
     * player's profile.
     *
     * One flow shared by every observer, started by the first and kept running for the life of the
     * process. Its initial value is available synchronously from [StateFlow.value]:
     * [SessionState.Loading] when Firebase has a persisted user still to restore, otherwise
     * [SessionState.SignedOut].
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
     * The session changes the user asked for — signing in, completing sign-up, signing out — each
     * reported once. See [SessionEvent].
     *
     * There is no replay: a collector receives only the events emitted while it is subscribed.
     * Emitting never waits on collectors, so an event that finds the buffer full is dropped.
     */
    val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents.asSharedFlow()

    /**
     * Signs in to an existing account with an email address and password, then reads the user's
     * player profile.
     *
     * Runs to completion even if the caller's scope is cancelled. A sign-in that ends in
     * [SessionState.SignedIn] is reported on [sessionEvents].
     *
     * @param email The account's email address.
     * @param password The account's password.
     * @return [SessionState.SignedIn] for an account with a player profile, or
     *   [SessionState.ProfileIncomplete] for one whose sign-up was never completed. Any failure of
     *   `AuthRepository.signIn`, or the failure to read the profile when it can't be read.
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

        // No profile means the sign-up never finished. A read that *failed* is reported instead —
        // answering ProfileIncomplete on a network blip would ask someone to create a profile they
        // already have.
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
     * Starts a sign-up by creating a Firebase Auth account and signing the new user in.
     *
     * The user has no player profile until [completeSignUp] succeeds. Runs to completion even if
     * the caller's scope is cancelled.
     *
     * @param email The new account's email address.
     * @param password The new account's password.
     * @return [SessionState.ProfileIncomplete] for the new user. Any failure of
     *   `AuthRepository.createAccount`, including [AuthErrors.EMAIL_IN_USE] when the address already
     *   has an account.
     */
    suspend fun startSignUp(email: String, password: String): TtcResult<SessionState.ProfileIncomplete> =
        nonCancelling {
            authRepository.createAccount(email, password)
                .map { SessionState.ProfileIncomplete(it.id, it.email) }
        }

    /**
     * Completes a sign-up by saving the signed-in user's player profile.
     *
     * [sessionState] becomes [SessionState.SignedIn] as soon as the profile is saved, before this
     * returns; the call runs to completion even for callers whose scope is cancelled by that state
     * change. Success is reported on [sessionEvents].
     *
     * @param name The player's name. Must not be blank.
     * @param phone The player's phone number, in any format. Must be a complete 10-digit number no
     *   other player has.
     * @param photo The avatar as JPEG bytes, already downscaled, or null for none.
     * @return The new player. [AuthErrors.SESSION_EXPIRED] when nobody is signed in. Otherwise any
     *   failure of `PlayerRepository.createPlayer`.
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

        // The user id was already set when the account was created, so events logged before the
        // profile was saved are attributed. Only the completion event waits for the profile.
        eventManager.logEvent(AnalyticsEvent.CreateAccount)

        return TtcResult.Success(player)
    }

    /**
     * Abandons a sign-up whose player profile was never saved, signing the user out and deleting
     * their account.
     *
     * Safe to call at any time, even after the sign-up completed: a user with a player profile is
     * left signed in and untouched. Otherwise the user is signed out, and the account is deleted,
     * best effort, only once its profile is confirmed missing. Returns at once; the outcome shows
     * only as a change to [sessionState], with nothing emitted on [sessionEvents].
     *
     * @param reason Why the sign-up was abandoned, logged with the abandonment, or null.
     */
    fun abandonSignUp(reason: String? = null) {
        externalScope.launch {
            val user = authRepository.currentUser
            if (user != null) {
                when (val profile = playerRepository.getPlayer(user.id)) {
                    is TtcLookup.Success -> {
                        // Already provisioned: the sign-up finished, so there is nothing to
                        // abandon.
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
     * Signs the current user out.
     *
     * Returns at once; the sign-out runs to completion even if the caller's scope is cancelled, and
     * is then reported on [sessionEvents]. Observe [sessionState] for the result.
     */
    fun signOut() {
        externalScope.launch {
            authRepository.signOut()
            _sessionEvents.tryEmit(SessionEvent.SignedOut)
        }
    }

    private var refreshJob: Job? = null

    /**
     * Revalidates the signed-in user with Firebase, signing out only if Firebase rejects the
     * account — disabled, deleted, or its tokens expired or revoked.
     *
     * Any other failure, such as no network, keeps the session. Returns at once; the refresh waits
     * until [sessionState] leaves [SessionState.Loading]. A call made while an earlier refresh is
     * still pending is ignored.
     */
    fun refreshSession() {
        if (refreshJob?.isActive == true) return
        refreshJob = externalScope.launch {
            // A forced refresh hands Firestore a new token, and Firestore restarts its connection
            // to use it; doing that while the profile is still loading holds up the first snapshot
            // by several seconds. Offline, the wait lasts until the profile can be read, which is
            // also the first moment a refresh could succeed.
            sessionState.first { it !is SessionState.Loading }
            authRepository.refreshAuthentication()
        }
    }

    /**
     * Runs [block] in [externalScope] and awaits its result.
     *
     * If the caller is cancelled — often by the change [block] itself makes to [sessionState] —
     * only the wait is abandoned, and [block] runs to completion.
     */
    private suspend fun <T> nonCancelling(block: suspend () -> T): T =
        externalScope.async { block() }.await()
}
