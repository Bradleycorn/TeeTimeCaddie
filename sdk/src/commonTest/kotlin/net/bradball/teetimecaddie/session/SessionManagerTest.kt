package net.bradball.teetimecaddie.session

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.features.auth.AuthErrors
import net.bradball.teetimecaddie.features.auth.AuthException
import net.bradball.teetimecaddie.features.auth.AuthUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class) // runCurrent
class SessionManagerTest {

    private val dana = Player(
        id = "user-1",
        name = "Dana Park",
        email = "dana@golf.app",
        phone = "5550101010"
    )

    /**
     * Built on the test's `backgroundScope`, which shares the test scheduler — so the work
     * [SessionManager] runs in its external scope can be driven with `runCurrent()` — but is
     * cancelled when the test ends. It has to be: the shared `sessionState` keeps collecting once
     * started, and on the test's own scope `runTest` would wait on it forever.
     */
    private fun TestScope.manager(
        auth: FakeAuthRepository = FakeAuthRepository(),
        players: FakePlayerRepository = FakePlayerRepository()
    ) = SessionManager(auth, players, EventManager.getInstance(enableLogging = false), backgroundScope)

    /** Everything [SessionManager.sessionEvents] emits from here on, collected in the background. */
    private fun TestScope.eventsOf(manager: SessionManager): List<SessionEvent> {
        val events = mutableListOf<SessionEvent>()
        backgroundScope.launch { manager.sessionEvents.toList(events) }
        testScheduler.runCurrent()
        return events
    }

    /** The first resolved session — skipping the synchronous [SessionState.Loading] seed. */
    private suspend fun SessionManager.resolvedState() = sessionState.first { it !is SessionState.Loading }

    // ── sessionState ────────────────────────────────────────────────────────────

    @Test
    fun sessionState_isSignedOutWithNoUser() = runTest {
        assertEquals(SessionState.SignedOut, manager().resolvedState())
    }

    @Test
    fun sessionState_isSignedInWhenTheUserHasAProfile() = runTest {
        val session = manager(
            auth = FakeAuthRepository(AuthUser(dana.id, dana.email)),
            players = FakePlayerRepository(dana)
        )

        val state = session.resolvedState()

        assertIs<SessionState.SignedIn>(state)
        assertEquals(dana, state.player)
    }

    // The state that only exists because account creation happens on the credentials screen: a
    // real session with no profile behind it.
    @Test
    fun sessionState_isProfileIncompleteWhenTheUserHasNoProfile() = runTest {
        val session = manager(auth = FakeAuthRepository(AuthUser("user-1", "new@golf.app")))

        val state = session.resolvedState()

        assertIs<SessionState.ProfileIncomplete>(state)
        assertEquals("user-1", state.userId)
        assertEquals("new@golf.app", state.email)
    }

    // Seeding with SignedOut when a session exists is what causes the cold-start flash of the
    // credentials screen, so the distinction matters. Read before anything has collected.
    @Test
    fun sessionState_startsLoadingWhenASessionExistsAndSignedOutOtherwise() = runTest {
        assertEquals(SessionState.SignedOut, manager().sessionState.value)

        val withUser = manager(auth = FakeAuthRepository(AuthUser(dana.id, dana.email)))
        assertEquals(SessionState.Loading, withUser.sessionState.value)
    }

    // The app root and the Profile screen both observe the session; they must share one Firestore
    // listener rather than each opening their own.
    @Test
    fun sessionState_isSharedByEveryObserver() = runTest {
        val players = FakePlayerRepository(dana)
        val session = manager(FakeAuthRepository(AuthUser(dana.id, dana.email)), players)

        backgroundScope.launch { session.sessionState.collect {} }
        backgroundScope.launch { session.sessionState.collect {} }
        runCurrent()

        assertEquals(1, players.playerFlowSubscriptions)
        assertIs<SessionState.SignedIn>(session.sessionState.value)
    }

    // ── signIn ──────────────────────────────────────────────────────────────────

    @Test
    fun signIn_returnsSignedInForAProvisionedAccount() = runTest {
        val auth = FakeAuthRepository()
        auth.signInResult = TtcResult.Success(AuthUser(dana.id, dana.email))

        val result = manager(auth, FakePlayerRepository(dana)).signIn(dana.email, "fairway")

        val state = assertIs<TtcResult.Success<SessionState>>(result).data
        assertIs<SessionState.SignedIn>(state)
        assertEquals(dana, state.player)
    }

    // Someone who force-quit mid-sign-up and came back later must resume the profile step, not
    // land on an empty Games tab.
    @Test
    fun signIn_returnsProfileIncompleteWhenTheAccountHasNoProfile() = runTest {
        val auth = FakeAuthRepository()
        auth.signInResult = TtcResult.Success(AuthUser("user-2", "half@golf.app"))

        val result = manager(auth).signIn("half@golf.app", "fairway")

        val state = assertIs<TtcResult.Success<SessionState>>(result).data
        assertIs<SessionState.ProfileIncomplete>(state)
        assertEquals("user-2", state.userId)
    }

    @Test
    fun signIn_propagatesTheAuthFailure() = runTest {
        val auth = FakeAuthRepository()
        auth.signInResult = TtcResult.Failure(AuthException(AuthErrors.INVALID_CREDENTIALS))

        val result = manager(auth).signIn("dana@golf.app", "wrong")

        val error = assertIs<TtcResult.Failure>(result).error
        assertEquals(AuthErrors.INVALID_CREDENTIALS, assertIs<AuthException>(error).error)
    }

    // ── completeSignUp ──────────────────────────────────────────────────────────

    @Test
    fun completeSignUp_savesTheProfileAndSyncsTheDisplayName() = runTest {
        val auth = FakeAuthRepository(AuthUser("user-3", "new@golf.app"))
        val players = FakePlayerRepository()

        val result = manager(auth, players).completeSignUp("Sam Pruitt", "5025551234")

        val player = assertIs<TtcResult.Success<net.bradball.teetimecaddie.core.models.Player>>(result).data
        assertEquals("Sam Pruitt", player.name)
        assertEquals("new@golf.app", player.email, "email comes from the auth record, not the form")
        assertEquals(player, players.getPlayer("user-3").getOrNull())
        assertEquals(listOf("Sam Pruitt"), auth.displayNameUpdates)
    }

    @Test
    fun completeSignUp_failsWhenTheSessionWentAway() = runTest {
        val result = manager().completeSignUp("Sam Pruitt", "5025551234")

        val error = assertIs<TtcResult.Failure>(result).error
        assertEquals(AuthErrors.SESSION_EXPIRED, assertIs<AuthException>(error).error)
    }

    // ── abandonSignUp ───────────────────────────────────────────────────────────

    @Test
    fun abandonSignUp_deletesAnAccountThatNeverGotAProfile() = runTest {
        val auth = FakeAuthRepository(AuthUser("user-4", "abandoned@golf.app"))

        manager(auth).abandonSignUp()
        runCurrent()

        assertEquals(1, auth.deleteCount)
        assertEquals(1, auth.signOutCount)
    }

    // The guard that makes abandonSignUp safe to call from anywhere — a back press, a
    // "Sign in instead" tap, the profile screen disappearing as the tree swaps after sign-up
    // succeeded. Without it, that last one would delete — or sign out of — the account just made.
    @Test
    fun abandonSignUp_leavesAProvisionedAccountAlone() = runTest {
        val auth = FakeAuthRepository(AuthUser(dana.id, dana.email))

        manager(auth, FakePlayerRepository(dana)).abandonSignUp()
        runCurrent()

        assertEquals(0, auth.deleteCount, "a player with a profile must never be deleted")
        assertEquals(0, auth.signOutCount, "a player with a profile must not be signed out")
    }

    // Firebase refuses deletion for a session old enough to need re-authentication. The orphaned
    // auth user is harmless, so this must not surface as an error to someone pressing Back.
    @Test
    fun abandonSignUp_stillSignsOutWhenDeletionIsRefused() = runTest {
        val auth = FakeAuthRepository(AuthUser("user-5", "stale@golf.app"))
        auth.deleteSucceeds = false

        manager(auth).abandonSignUp()
        runCurrent()

        assertEquals(1, auth.deleteCount)
        assertEquals(1, auth.signOutCount)
    }

    // A failed profile read is not evidence that the profile is absent. Conflating the two here
    // would delete a real account, which is exactly what the guard exists to prevent.
    @Test
    fun abandonSignUp_doesNotDeleteWhenTheProfileReadFails() = runTest {
        val auth = FakeAuthRepository(AuthUser(dana.id, dana.email))
        val players = FakePlayerRepository(dana)
        players.readFailure = readFailure()

        manager(auth, players).abandonSignUp()
        runCurrent()

        assertEquals(0, auth.deleteCount, "a failed read must never be treated as 'no profile'")
        assertEquals(1, auth.signOutCount)
    }

    // Same distinction on the sign-in path: routing to the profile step on a network blip would
    // invite someone to re-enter details over a profile that already exists.
    @Test
    fun signIn_reportsAFailedProfileReadInsteadOfRoutingToTheProfileStep() = runTest {
        val auth = FakeAuthRepository()
        auth.signInResult = TtcResult.Success(AuthUser(dana.id, dana.email))
        val players = FakePlayerRepository(dana)
        players.readFailure = readFailure()

        val result = manager(auth, players).signIn(dana.email, "fairway")

        assertIs<TtcResult.Failure>(result)
    }

    @Test
    fun completeSignUp_propagatesAProfileFailure() = runTest {
        val auth = FakeAuthRepository(AuthUser("user-6", "new@golf.app"))
        val players = FakePlayerRepository()
        players.createFailure = phoneInUseFailure()

        val result = manager(auth, players).completeSignUp("Sam Pruitt", "5551234567")

        assertIs<TtcResult.Failure>(result)
        assertTrue(auth.displayNameUpdates.isEmpty(), "nothing should be synced after a failure")
    }

    @Test
    fun abandonSignUp_withNoUserIsANoOpThatStillSignsOut() = runTest {
        val auth = FakeAuthRepository()

        manager(auth).abandonSignUp()
        runCurrent()

        assertEquals(0, auth.deleteCount)
        assertTrue(auth.signOutCount == 1)
    }

    // ── cancellation ────────────────────────────────────────────────────────────

    // Saving the profile flips the session to SignedIn, which destroys the calling screen while
    // completeSignUp still has work to do. That work must finish anyway.
    @Test
    fun completeSignUp_finishesAfterItsCallerIsCancelled() = runTest {
        val auth = FakeAuthRepository(AuthUser("user-7", "new@golf.app"))
        val players = FakePlayerRepository()
        val gate = CompletableDeferred<Unit>()
        players.createGate = gate
        val session = manager(auth, players)
        val events = eventsOf(session)

        val caller = launch { session.completeSignUp("Sam Pruitt", "5025551234") }
        runCurrent()
        caller.cancel()
        gate.complete(Unit)
        runCurrent()

        assertTrue(caller.isCancelled)
        assertEquals(listOf("Sam Pruitt"), auth.displayNameUpdates)
        assertIs<SessionEvent.AccountCreated>(events.single())
    }

    // ── sessionEvents ───────────────────────────────────────────────────────────

    @Test
    fun signIn_reportsSignedIn() = runTest {
        val auth = FakeAuthRepository()
        auth.signInResult = TtcResult.Success(AuthUser(dana.id, dana.email))
        val session = manager(auth, FakePlayerRepository(dana))
        val events = eventsOf(session)

        session.signIn(dana.email, "fairway")
        runCurrent()

        assertEquals(listOf<SessionEvent>(SessionEvent.SignedIn(dana)), events)
    }

    // Resuming an interrupted sign-up is not "welcome back": the person is not signed in yet.
    @Test
    fun signIn_intoAnIncompleteProfileReportsNothing() = runTest {
        val auth = FakeAuthRepository()
        auth.signInResult = TtcResult.Success(AuthUser("user-2", "half@golf.app"))
        val session = manager(auth)
        val events = eventsOf(session)

        session.signIn("half@golf.app", "fairway")
        runCurrent()

        assertTrue(events.isEmpty())
    }

    @Test
    fun failedCalls_reportNothing() = runTest {
        val auth = FakeAuthRepository(AuthUser("user-6", "new@golf.app"))
        auth.signInResult = TtcResult.Failure(AuthException(AuthErrors.INVALID_CREDENTIALS))
        val players = FakePlayerRepository()
        players.createFailure = phoneInUseFailure()
        val session = manager(auth, players)
        val events = eventsOf(session)

        session.signIn("dana@golf.app", "wrong")
        session.completeSignUp("Sam Pruitt", "5551234567")
        runCurrent()

        assertTrue(events.isEmpty())
    }

    @Test
    fun completeSignUp_reportsAccountCreated() = runTest {
        val auth = FakeAuthRepository(AuthUser("user-3", "new@golf.app"))
        val session = manager(auth, FakePlayerRepository())
        val events = eventsOf(session)

        val player = assertIs<TtcResult.Success<Player>>(session.completeSignUp("Sam Pruitt", "5025551234")).data
        runCurrent()

        assertEquals(listOf<SessionEvent>(SessionEvent.AccountCreated(player)), events)
    }

    @Test
    fun signOut_reportsSignedOut() = runTest {
        val session = manager(FakeAuthRepository(AuthUser(dana.id, dana.email)), FakePlayerRepository(dana))
        val events = eventsOf(session)

        session.signOut()
        runCurrent()

        assertEquals(listOf<SessionEvent>(SessionEvent.SignedOut), events)
    }

    // Backing out of sign-up is not a sign-out anyone should be told about.
    @Test
    fun abandonSignUp_reportsNothing() = runTest {
        val session = manager(FakeAuthRepository(AuthUser("user-4", "abandoned@golf.app")))
        val events = eventsOf(session)

        session.abandonSignUp()
        runCurrent()

        assertTrue(events.isEmpty())
    }
}
