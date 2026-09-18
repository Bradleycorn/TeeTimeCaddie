package net.bradball.teetimecaddie.features.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.LoggableExceptionTypes
import net.bradball.teetimecaddie.core.extensions.isValidEmail
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
 */
interface AuthRepository {

    /** The signed-in user, or null. Reads Firebase's restored session synchronously. */
    val currentUser: AuthUser?

    /** Emits on every sign-in and sign-out, starting with the current value. */
    val authChanges: Flow<AuthUser?>

    @Throws(AuthException::class, CancellationException::class)
    suspend fun signIn(email: String, password: String): AuthUser

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
    @Throws(AuthException::class, CancellationException::class)
    suspend fun createAccount(email: String, password: String): AuthUser

    suspend fun signOut()

    /**
     * Delete the signed-in account. Used to clean up a sign-up the person abandoned.
     *
     * Fails for a session Firebase considers stale, which it will for an account created long
     * enough ago to need re-authentication. Callers should treat failure as non-fatal.
     */
    @Throws(AuthException::class, CancellationException::class)
    suspend fun deleteCurrentUser()

    /** Force a token refresh, signing out if the session is no longer valid. */
    suspend fun refreshAuthentication()

    /** Keep the Firebase Auth record's display name in step with the player's profile. */
    suspend fun updateDisplayName(name: String)
}

class AuthRepositoryImpl(
    private val eventManager: EventManager
) : AuthRepository {

    override val currentUser: AuthUser?
        get() = Firebase.auth.currentUser?.toAuthUser()

    override val authChanges: Flow<AuthUser?>
        get() = Firebase.auth.authStateChanged.map { it?.toAuthUser() }

    override suspend fun signIn(email: String, password: String): AuthUser {
        if (!email.isValidEmail) {
            throw AuthException(AuthErrors.INVALID_EMAIL)
        }

        val user = try {
            Firebase.auth.signInWithEmailAndPassword(email, password).user
                ?: throw IllegalStateException("No user available after a successful sign in.")
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            val error = signInErrorFor(ex.firebaseAuthErrorCode())
            if (error == AuthErrors.UNKNOWN) {
                eventManager.logException(
                    ex, LoggableExceptionTypes.AUTHENTICATION, hashMapOf("action" to "sign_in")
                )
            }
            eventManager.logEvent(AnalyticsEvent.FailedLogin(reason = error.name))
            throw AuthException(error, listOf(email), ex)
        }

        // Both of these were missing before: sign-in recorded neither the user id nor the event,
        // so every signed-in session was attributed to nobody.
        eventManager.setUserId(user.uid)
        eventManager.logEvent(AnalyticsEvent.Login)

        return user.toAuthUser()
    }

    override suspend fun createAccount(email: String, password: String): AuthUser {
        // Validate locally first, so a password Firebase would reject never creates an account.
        if (!email.isValidEmail) {
            eventManager.logEvent(AnalyticsEvent.FailedRegistration(AuthErrors.INVALID_EMAIL.name))
            throw AuthException(AuthErrors.INVALID_EMAIL)
        }
        if (!password.isStrongEnough) {
            eventManager.logEvent(AnalyticsEvent.FailedRegistration(AuthErrors.WEAK_PASSWORD.name))
            throw AuthException(AuthErrors.WEAK_PASSWORD)
        }

        val user = try {
            Firebase.auth.createUserWithEmailAndPassword(email, password).user
                ?: throw IllegalStateException("No user available after a successful registration.")
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            val error = createAccountErrorFor(ex.firebaseAuthErrorCode())
            if (error == AuthErrors.UNKNOWN) {
                eventManager.logException(
                    ex, LoggableExceptionTypes.AUTHENTICATION, hashMapOf("action" to "create_account")
                )
            }
            eventManager.logEvent(AnalyticsEvent.FailedRegistration(error.name))
            throw AuthException(error, listOf(email), ex)
        }

        // Note what is NOT logged here: the account is not real until it has a profile, so
        // CreateAccount and setUserId wait for SessionManager.completeSignUp.
        eventManager.logEvent(AnalyticsEvent.CreateAccountStarted)

        return user.toAuthUser()
    }

    override suspend fun signOut() {
        Firebase.auth.signOut()
        eventManager.logEvent(AnalyticsEvent.SignOut)
    }

    override suspend fun deleteCurrentUser() {
        val user = Firebase.auth.currentUser ?: throw AuthException(AuthErrors.SESSION_EXPIRED)
        try {
            user.delete()
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            throw AuthException(signInErrorFor(ex.firebaseAuthErrorCode()), cause = ex)
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
        Firebase.auth.currentUser?.updateProfile(displayName = name)
    }
}

/** At least 8 characters with at least one digit — the rule the sign-up copy promises. */
private val String.isStrongEnough: Boolean
    get() = length >= 8 && any { it.isDigit() }

private fun dev.gitlive.firebase.auth.FirebaseUser.toAuthUser() = AuthUser(
    id = uid,
    email = email.orEmpty()
)
