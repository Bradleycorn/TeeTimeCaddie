package net.bradball.teetimecaddie.session

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.NoOpErrorLogger
import net.bradball.teetimecaddie.core.analytics.NoOpTransactionLogger
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.features.auth.AuthErrors
import net.bradball.teetimecaddie.features.auth.AuthException
import net.bradball.teetimecaddie.features.auth.AuthUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SessionManagerTest {

    private val dana = Player(
        id = "user-1",
        name = "Dana Park",
        email = "dana@golf.app",
        phone = "5550101010"
    )

    private fun manager(
        auth: FakeAuthRepository = FakeAuthRepository(),
        players: FakePlayerRepository = FakePlayerRepository()
    ) = SessionManager(auth, players, EventManager(NoOpErrorLogger, NoOpTransactionLogger))

    // ── sessionState ────────────────────────────────────────────────────────────

    @Test
    fun sessionState_isSignedOutWithNoUser() = runTest {
        assertEquals(SessionState.SignedOut, manager().sessionState.first())
    }

    @Test
    fun sessionState_isSignedInWhenTheUserHasAProfile() = runTest {
        val session = manager(
            auth = FakeAuthRepository(AuthUser(dana.id, dana.email)),
            players = FakePlayerRepository(dana)
        )

        val state = session.sessionState.first()

        assertIs<SessionState.SignedIn>(state)
        assertEquals(dana, state.player)
    }

    // The state that only exists because account creation happens on the credentials screen: a
    // real session with no profile behind it.
    @Test
    fun sessionState_isProfileIncompleteWhenTheUserHasNoProfile() = runTest {
        val session = manager(auth = FakeAuthRepository(AuthUser("user-1", "new@golf.app")))

        val state = session.sessionState.first()

        assertIs<SessionState.ProfileIncomplete>(state)
        assertEquals("user-1", state.userId)
        assertEquals("new@golf.app", state.email)
    }

    // ── initialSessionState ─────────────────────────────────────────────────────

    // Seeding with SignedOut when a session exists is what causes the cold-start flash of the
    // credentials screen, so the distinction matters.
    @Test
    fun initialSessionState_isLoadingWhenASessionExistsAndSignedOutOtherwise() {
        assertEquals(SessionState.SignedOut, manager().initialSessionState)

        val withUser = manager(auth = FakeAuthRepository(AuthUser(dana.id, dana.email)))
        assertEquals(SessionState.Loading, withUser.initialSessionState)
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

        assertEquals(1, auth.deleteCount)
        assertEquals(1, auth.signOutCount)
    }

    // The guard that makes abandonSignUp safe to call from anywhere — a back press, a
    // "Sign in instead" tap. Without it, tapping back on the profile step after a completed
    // sign-up would delete a real account.
    @Test
    fun abandonSignUp_neverDeletesAProvisionedAccount() = runTest {
        val auth = FakeAuthRepository(AuthUser(dana.id, dana.email))

        manager(auth, FakePlayerRepository(dana)).abandonSignUp()

        assertEquals(0, auth.deleteCount, "a player with a profile must never be deleted")
        assertEquals(1, auth.signOutCount)
    }

    // Firebase refuses deletion for a session old enough to need re-authentication. The orphaned
    // auth user is harmless, so this must not surface as an error to someone pressing Back.
    @Test
    fun abandonSignUp_stillSignsOutWhenDeletionIsRefused() = runTest {
        val auth = FakeAuthRepository(AuthUser("user-5", "stale@golf.app"))
        auth.deleteSucceeds = false

        manager(auth).abandonSignUp()

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

        assertEquals(0, auth.deleteCount)
        assertTrue(auth.signOutCount == 1)
    }
}
