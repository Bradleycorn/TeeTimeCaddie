package net.bradball.teetimecaddie.session

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.NoOpErrorLogger
import net.bradball.teetimecaddie.core.analytics.NoOpTransactionLogger
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.features.auth.AuthErrors
import net.bradball.teetimecaddie.features.auth.AuthException
import net.bradball.teetimecaddie.features.auth.AuthUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
        auth.signInResult = Result.success(AuthUser(dana.id, dana.email))

        val state = manager(auth, FakePlayerRepository(dana)).signIn(dana.email, "fairway")

        assertIs<SessionState.SignedIn>(state)
        assertEquals(dana, state.player)
    }

    // Someone who force-quit mid-sign-up and came back later must resume the profile step, not
    // land on an empty Games tab.
    @Test
    fun signIn_returnsProfileIncompleteWhenTheAccountHasNoProfile() = runTest {
        val auth = FakeAuthRepository()
        auth.signInResult = Result.success(AuthUser("user-2", "half@golf.app"))

        val state = manager(auth).signIn("half@golf.app", "fairway")

        assertIs<SessionState.ProfileIncomplete>(state)
        assertEquals("user-2", state.userId)
    }

    @Test
    fun signIn_propagatesTheAuthFailure() = runTest {
        val auth = FakeAuthRepository()
        auth.signInResult = Result.failure(AuthException(AuthErrors.INVALID_CREDENTIALS))

        val thrown = assertFailsWith<AuthException> {
            manager(auth).signIn("dana@golf.app", "wrong")
        }
        assertEquals(AuthErrors.INVALID_CREDENTIALS, thrown.error)
    }

    // ── completeSignUp ──────────────────────────────────────────────────────────

    @Test
    fun completeSignUp_savesTheProfileAndSyncsTheDisplayName() = runTest {
        val auth = FakeAuthRepository(AuthUser("user-3", "new@golf.app"))
        val players = FakePlayerRepository()

        val player = manager(auth, players).completeSignUp("Sam Pruitt", "5025551234")

        assertEquals("Sam Pruitt", player.name)
        assertEquals("new@golf.app", player.email, "email comes from the auth record, not the form")
        assertEquals(player, players.getPlayer("user-3"))
        assertEquals(listOf("Sam Pruitt"), auth.displayNameUpdates)
    }

    @Test
    fun completeSignUp_failsWhenTheSessionWentAway() = runTest {
        val thrown = assertFailsWith<AuthException> {
            manager().completeSignUp("Sam Pruitt", "5025551234")
        }
        assertEquals(AuthErrors.SESSION_EXPIRED, thrown.error)
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
        auth.deleteShouldFail = true

        manager(auth).abandonSignUp()

        assertEquals(1, auth.deleteCount)
        assertEquals(1, auth.signOutCount)
    }

    @Test
    fun abandonSignUp_withNoUserIsANoOpThatStillSignsOut() = runTest {
        val auth = FakeAuthRepository()

        manager(auth).abandonSignUp()

        assertEquals(0, auth.deleteCount)
        assertTrue(auth.signOutCount == 1)
    }
}
