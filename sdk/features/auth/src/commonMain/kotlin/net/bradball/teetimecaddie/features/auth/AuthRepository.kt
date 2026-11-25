package net.bradball.teetimecaddie.features.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.LoggableExceptionTypes
import net.bradball.teetimecaddie.core.extensions.isValidEmail
import net.bradball.teetimecaddie.core.models.TtcResult
import kotlin.coroutines.coroutineContext

/**
 * Firebase Auth accounts: creating them, signing in and out, and keeping the session current.
 *
 * Profile data — a person's name, phone number and avatar — belongs to `PlayerRepository`, and
 * joining a signed-in user to their profile is `SessionManager`'s job.
 *
 * **Nothing here throws.** Failures come back as [TtcResult.Failure] carrying an [AuthException],
 * because Kotlin exceptions cross into Swift badly. See [TtcResult].
 */
interface AuthRepository {

    /** The signed-in user, or null. Reads Firebase's restored session synchronously. */
    val currentUser: AuthUser?

    /** The signed-in user, emitted on every sign-in and sign-out, starting with the current value. */
    val authChanges: Flow<AuthUser?>

    /**
     * Signs in to an existing account with an email address and password.
     *
     * @param email The account's email address.
     * @param password The account's password.
     * @return The signed-in user. [AuthErrors.INVALID_EMAIL] for a malformed address, checked
     *   before contacting Firebase. [AuthErrors.INVALID_CREDENTIALS] for an unknown address or a
     *   wrong password, which can't be told apart. Otherwise [AuthErrors.ACCOUNT_DISABLED],
     *   [AuthErrors.TOO_MANY_ATTEMPTS], [AuthErrors.NETWORK], [AuthErrors.SESSION_EXPIRED] or
     *   [AuthErrors.UNKNOWN].
     */
    suspend fun signIn(email: String, password: String): TtcResult<AuthUser>

    /**
     * Creates a Firebase Auth account and signs the new user in.
     *
     * Creates only the account, not a player profile: afterwards there is a signed-in user with no
     * profile until one is created with `PlayerRepository.createPlayer`. This is also the only call
     * that reports an address already has an account.
     *
     * @param email The new account's email address.
     * @param password The new account's password: at least 8 characters, including a digit.
     * @return The new, signed-in user. [AuthErrors.INVALID_EMAIL] for a malformed address and
     *   [AuthErrors.WEAK_PASSWORD] for a password that's too weak, both checked before an account
     *   is created. [AuthErrors.EMAIL_IN_USE] when the address already has an account. Otherwise
     *   [AuthErrors.TOO_MANY_ATTEMPTS], [AuthErrors.NETWORK] or [AuthErrors.UNKNOWN].
     */
    suspend fun createAccount(email: String, password: String): TtcResult<AuthUser>

    /** Signs the current user out. Does nothing when nobody is signed in. */
    suspend fun signOut()

    /**
     * Deletes the signed-in user's Firebase Auth account. Their player profile, if any, is left
     * alone.
     *
     * @return True when the account was deleted. False when nobody is signed in, or Firebase
     *   refused — which it does for a session old enough to need re-authentication. Failing to
     *   delete is reported rather than treated as an error.
     */
    suspend fun deleteCurrentUser(): Boolean

    /**
     * Refreshes the signed-in user's token, signing out only if Firebase rejects the account —
     * disabled, deleted, or its tokens expired or revoked.
     *
     * Any other failure, a missing network above all, leaves the session alone: it says nothing
     * about the account, and the next refresh will try again. Does nothing when nobody is signed
     * in.
     */
    suspend fun refreshAuthentication()

    /**
     * Sets the signed-in user's Firebase Auth display name, to keep it in step with their player
     * profile.
     *
     * Best effort: a failure is logged and otherwise ignored, since the profile, not the auth
     * record, is the source of truth for the name.
     *
     * @param name The player's name.
     */
    suspend fun updateDisplayName(name: String)
}

/**
 * The [AuthRepository] the apps use. It also reports to analytics:
 * - The user id, set on every successful sign-in and as soon as an account is created.
 * - [AnalyticsEvent.Login] and [AnalyticsEvent.FailedLogin] for sign-in attempts.
 * - [AnalyticsEvent.CreateAccountStarted] and [AnalyticsEvent.FailedRegistration] for account
 *   creation. The `CreateAccount` event waits for the player profile, so it isn't logged here.
 * - [AnalyticsEvent.SignOut] on sign-out.
 */
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
        } catch (ex: Exception) {
            // Re-throws if THIS coroutine was cancelled, so cancellation keeps propagating instead
            // of being swallowed into a Failure. Better than catching CancellationException by
            // type: that can also arrive from a child scope while this coroutine is alive and
            // well, in which case it is a genuine failure and should be reported as one.
            currentCoroutineContext().ensureActive()
            return TtcResult.Failure(authFailure(ex, AuthErrors::fromSignInErrorCode, "sign_in", email))
        }

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
        } catch (ex: Exception) {
            currentCoroutineContext().ensureActive()
            return TtcResult.Failure(authFailure(ex, AuthErrors::fromCreateAccountErrorCode, "create_account", email))
        }

        // Attribute from here on. The person is authenticated the moment the account exists, and
        // everything logged during the profile step — PHONE_IN_USE, PHOTO_UPLOAD_FAILED,
        // AbandonedRegistration — belongs to them. That step is where sign-up drop-off happens,
        // so it is the window most worth attributing.
        eventManager.setUserId(user.uid)

        // The CreateAccount *event* still waits for the profile: the account is not real as a
        // product concept until it has one, and CreateAccountStarted -> CreateAccount is what
        // measures completion of the second step.
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
        } catch (ex: Exception) {
            currentCoroutineContext().ensureActive()
            eventManager.logException(
                ex, LoggableExceptionTypes.AUTHENTICATION, hashMapOf("action" to "delete_user")
            )
            false
        }
    }

    override suspend fun refreshAuthentication() {
        try {
            Firebase.auth.currentUser?.getIdToken(forceRefresh = true)
        } catch (ex: Exception) {
            currentCoroutineContext().ensureActive()
            when {
                AuthErrors.rejectsSession(ex.firebaseAuthErrorCode()) -> Firebase.auth.signOut()

                // Expected whenever the app opens offline; not worth reporting.
                ex is FirebaseNetworkException -> Unit

                else -> eventManager.logException(
                    ex, LoggableExceptionTypes.AUTHENTICATION, hashMapOf("action" to "refresh_authentication")
                )
            }
        }
    }

    override suspend fun updateDisplayName(name: String) {
        try {
            Firebase.auth.currentUser?.updateProfile(displayName = name)
        } catch (ex: Exception) {
            currentCoroutineContext().ensureActive()
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

/** True when the password has at least 8 characters, including at least one digit. */
private val String.isStrongEnough: Boolean
    get() = length >= 8 && any { it.isDigit() }

private fun dev.gitlive.firebase.auth.FirebaseUser.toAuthUser() = AuthUser(
    id = uid,
    email = email.orEmpty()
)
