package net.bradball.teetimecaddie.features.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuthEmailException
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.bradbal.teetimecaddie.core.storage.PlayerStorage
import net.bradbal.teetimecaddie.core.storage.documents.PlayerDocument
import net.bradbal.teetimecaddie.core.storage.settings.TeeTimeCaddieSettings
import net.bradbal.teetimecaddie.core.storage.settings.hasLoggedIn
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.LoggableExceptionTypes
import net.bradball.teetimecaddie.core.extensions.empty
import net.bradball.teetimecaddie.core.models.User
import kotlin.coroutines.cancellation.CancellationException

class AuthRepository(
    private val eventManager: EventManager,
    private val appSettings: TeeTimeCaddieSettings,
    private val playerStorage: PlayerStorage
) {

    val currentUser: User
        get() = Firebase.auth.currentUser?.let { fbUser ->
            User(fbUser.uid, fbUser.displayName ?: String.empty)
        } ?: User(String.empty, "Anonymous")
    
    val isLoggedIn: Boolean
        get() = Firebase.auth.currentUser != null

    val loginState: Flow<Boolean>
        get() = Firebase.auth.authStateChanged.map { it != null }

    val hasLoggedInOnce: Boolean
        get()  = appSettings.hasLoggedIn

    suspend fun login(email: String, password: String) {
        try {
            Firebase.auth.signInWithEmailAndPassword(email, password)
            eventManager.logEvent(AnalyticsEvent.Login)
        } catch (ex: Exception) {
            eventManager.logEvent(AnalyticsEvent.FailedLogin(reason = ex.message))
            throw AuthException(AuthErrors.INVALID_CREDENTIALS, ex)
        }
    }

    suspend fun refreshAuthentication() {
        try {
            Firebase.auth.currentUser?.getIdToken(forceRefresh = true)
        } catch (ex: Exception) {
            Firebase.auth.signOut()
        }
    }

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
            playerStorage.addPlayer(user.uid, PlayerDocument(name))
            appSettings.hasLoggedIn = true
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