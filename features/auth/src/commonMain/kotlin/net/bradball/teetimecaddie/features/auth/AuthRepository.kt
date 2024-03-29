package net.bradball.teetimecaddie.features.auth

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuthEmailException
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.LoggableExceptionTypes
import kotlin.coroutines.cancellation.CancellationException

class AuthRepository(
    private val eventManager: EventManager,
    private val authSettings: AuthSettings,
    useEmulator: Boolean = false
) {
    init {
        if (useEmulator) {
            Firebase.auth.useEmulator(authSettings.debugHost, authSettings.debugPort)
        }
    }
    private val currentUser: FirebaseUser?
        get() = Firebase.auth.currentUser

    val isLoggedIn: Boolean
        get() = currentUser != null

    @NativeCoroutines
    val loginState: Flow<Boolean>
        get() = Firebase.auth.authStateChanged.map { it != null }

    val hasLoggedInOnce: Boolean
        get() = authSettings.hasLoggedIn

    @NativeCoroutines
    suspend fun login(email: String, password: String) {
        try {
            Firebase.auth.signInWithEmailAndPassword(email, password)
            eventManager.logEvent(AnalyticsEvent.Login)
        } catch (ex: Exception) {
            eventManager.logEvent(AnalyticsEvent.FailedLogin(reason = ex.message))
            throw AuthException(AuthErrors.INVALID_CREDENTIALS, ex)
        }
    }

    @NativeCoroutines
    suspend fun refreshAuthentication() {
        try {
            Firebase.auth.currentUser?.getIdToken(forceRefresh = true)
        } catch (ex: Exception) {
            Firebase.auth.signOut()
        }
    }

    @NativeCoroutines
    @Throws(AuthException::class, CancellationException::class)
    suspend fun registerUser(email: String, password: String, name: String) {
        if (password.length < 8 || !password.contains("\\d".toRegex())) {
            eventManager.logEvent(AnalyticsEvent.FailedRegistration(AuthErrors.WEAK_PASSWORD.name))
            throw AuthException(AuthErrors.WEAK_PASSWORD)
        }

        if (name.isBlank()) {
            eventManager.logEvent((AnalyticsEvent.FailedRegistration(AuthErrors.NO_NAME.name)))
            throw AuthException(AuthErrors.NO_NAME)
        }

        try {
            val user = Firebase.auth.createUserWithEmailAndPassword(email, password).user
                ?: throw Exception("No user available after registration.")

            user.updateProfile(displayName = name)
            authSettings.hasLoggedIn = true
            eventManager.setUserId(user.uid)
            eventManager.logEvent(AnalyticsEvent.CreateAccount)
        } catch (ex: Exception) {
            val error = when (ex) {
                is FirebaseAuthUserCollisionException -> AuthErrors.USER_EXISTS
                is FirebaseAuthInvalidCredentialsException -> AuthErrors.INVALID_EMAIL
                is FirebaseAuthEmailException -> AuthErrors.INVALID_EMAIL
                is FirebaseAuthException -> AuthErrors.REG_DEFAULT
                else -> {
                    eventManager.logException(ex, LoggableExceptionTypes.AUTHENTICATION, hashMapOf("action" to "register"))
                    AuthErrors.UNKNOWN
                }
            }
            eventManager.logEvent(AnalyticsEvent.FailedRegistration(error.name))
            throw AuthException(error, ex)
        }
    }
}