package net.bradball.teetimecaddie.features.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.LoggableExceptionTypes
import net.bradball.teetimecaddie.core.extensions.isValidEmail
import net.bradball.teetimecaddie.core.models.TtcResult
import kotlin.coroutines.cancellation.CancellationException

/**
 * Credentials and sessions. Knows about Firebase Auth and nothing else.
 *
 * Profile data — a person's name, phone number and avatar — belongs to the players feature. That
 * separation is not tidiness: matching an invitation to a phone number has nothing to do with
 * authentication, and a repository that owned both would drag auth into every feature that needs
 * to look up a player.
 *
 * Nothing here knows what a "session" is in the app's sense either. Joining a signed-in user to
 * their profile is `SessionManager`'s job.
 *
 * **Nothing here throws.** Failures come back as [TtcResult.Failure] carrying an [AuthException],
 * because Kotlin exceptions cross into Swift badly. See [TtcResult].
 */
interface AuthRepository {

    /** The signed-in user, or null. Reads Firebase's restored session synchronously. */
    val currentUser: AuthUser?

    /** Emits on every sign-in and sign-out, starting with the current value. */
    val authChanges: Flow<AuthUser?>

    suspend fun signIn(email: String, password: String): TtcResult<AuthUser>

    /**
     * Create the Firebase Auth account.
     *
     * This is step one of a two-step sign-up, and it runs from the *credentials* screen rather
     * than after the profile step. That ordering is deliberate: `createUser` is the only call that
     * authoritatively reports an address is taken, and the story requires that rejection to land
     * before the person fills in their name and phone number.
     *
     * Firebase signs the new user in as a side effect, so afterwards there is an authenticated
     * user with no profile. `SessionManager` models that as a first-class state rather than
     * hiding it.
     */
    suspend fun createAccount(email: String, password: String): TtcResult<AuthUser>

    suspend fun signOut()

    /**
     * Delete the signed-in account. Used to clean up a sign-up the person abandoned.
     *
     * @return false if there was no account to delete, or Firebase refused — which it does for a
     *   session old enough to need re-authentication. Callers treat that as non-fatal, so this
     *   reports rather than fails.
     */
    suspend fun deleteCurrentUser(): Boolean

    /** Force a token refresh, signing out if the session is no longer valid. */
    suspend fun refreshAuthentication()

    /**
     * Keep the Firebase Auth record's display name in step with the player's profile.
     *
     * Best-effort: the auth record's name is a convenience, not a source of truth.
     */
    suspend fun updateDisplayName(name: String)
}

class AuthRepositoryImpl(
    private val eventManager: EventManager
) : AuthRepository {

    override val currentUser: AuthUser?
        get() = Firebase.auth.currentUser?.toAuthUser()

    override val authChanges: Flow<AuthUser?>
        get() = Firebase.auth.authStateChanged.map { it?.toAuthUser() }

    override suspend fun signIn(email: String, password: String): TtcResult<AuthUser> {
        if (!email.isValidEmail) {
            return TtcResult.Failure(AuthException(AuthErrors.INVALID_EMAIL))
        }

        val user = try {
            Firebase.auth.signInWithEmailAndPassword(email, password).user
                ?: throw IllegalStateException("No user available after a successful sign in.")
        } catch (ex: CancellationException) {
            // Cancellation is control flow, not a failure — let it propagate so the calling
            // coroutine unwinds normally instead of being reported as a sign-in error.
            throw ex
        } catch (ex: Exception) {
            return TtcResult.Failure(authFailure(ex, ::signInErrorFor, "sign_in", email))
        }

        // Both of these were missing before: sign-in recorded neither the user id nor the event,
        // so every signed-in session was attributed to nobody.
        eventManager.setUserId(user.uid)
        eventManager.logEvent(AnalyticsEvent.Login)

        return TtcResult.Success(user.toAuthUser())
    }

    override suspend fun createAccount(email: String, password: String): TtcResult<AuthUser> {
        // Validate locally first, so a password Firebase would reject never creates an account.
        if (!email.isValidEmail) {
            eventManager.logEvent(AnalyticsEvent.FailedRegistration(AuthErrors.INVALID_EMAIL.name))
            return TtcResult.Failure(AuthException(AuthErrors.INVALID_EMAIL))
        }
        if (!password.isStrongEnough) {
            eventManager.logEvent(AnalyticsEvent.FailedRegistration(AuthErrors.WEAK_PASSWORD.name))
            return TtcResult.Failure(AuthException(AuthErrors.WEAK_PASSWORD))
        }

        val user = try {
            Firebase.auth.createUserWithEmailAndPassword(email, password).user
                ?: throw IllegalStateException("No user available after a successful registration.")
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            return TtcResult.Failure(authFailure(ex, ::createAccountErrorFor, "create_account", email))
        }

        // Note what is NOT logged here: the account is not real until it has a profile, so
        // CreateAccount and setUserId wait for SessionManager.completeSignUp.
        eventManager.logEvent(AnalyticsEvent.CreateAccountStarted)

        return TtcResult.Success(user.toAuthUser())
    }

    override suspend fun signOut() {
        Firebase.auth.signOut()
        eventManager.logEvent(AnalyticsEvent.SignOut)
    }

    override suspend fun deleteCurrentUser(): Boolean {
        val user = Firebase.auth.currentUser ?: return false
        return try {
            user.delete()
            true
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            eventManager.logException(
                ex, LoggableExceptionTypes.AUTHENTICATION, hashMapOf("action" to "delete_user")
            )
            false
        }
    }

    override suspend fun refreshAuthentication() {
        try {
            Firebase.auth.currentUser?.getIdToken(forceRefresh = true)
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            Firebase.auth.signOut()
        }
    }

    override suspend fun updateDisplayName(name: String) {
        try {
            Firebase.auth.currentUser?.updateProfile(displayName = name)
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            eventManager.logException(
                ex, LoggableExceptionTypes.AUTHENTICATION, hashMapOf("action" to "update_display_name")
            )
        }
    }

    /** Map a thrown Firebase error to the [AuthException] that is handed back as a value. */
    private fun authFailure(
        ex: Exception,
        mapper: (String?) -> AuthErrors,
        action: String,
        email: String
    ): AuthException {
        val error = mapper(ex.firebaseAuthErrorCode())
        if (error == AuthErrors.UNKNOWN) {
            eventManager.logException(
                ex, LoggableExceptionTypes.AUTHENTICATION, hashMapOf("action" to action)
            )
        }
        val event = if (action == "sign_in") {
            AnalyticsEvent.FailedLogin(reason = error.name)
        } else {
            AnalyticsEvent.FailedRegistration(reason = error.name)
        }
        eventManager.logEvent(event)
        return AuthException(error, listOf(email), ex)
    }
}

/** At least 8 characters with at least one digit — the rule the sign-up copy promises. */
private val String.isStrongEnough: Boolean
    get() = length >= 8 && any { it.isDigit() }

private fun dev.gitlive.firebase.auth.FirebaseUser.toAuthUser() = AuthUser(
    id = uid,
    email = email.orEmpty()
)
