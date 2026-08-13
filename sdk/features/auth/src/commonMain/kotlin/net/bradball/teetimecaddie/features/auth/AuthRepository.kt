package net.bradball.teetimecaddie.features.auth

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
    private val playerStorage: PlayerStorage,
    private val authService: AuthService
) {

    val currentUser: User
        get() {
            val userId = authService.currentUserId
            return if (userId != null) {
                User(userId, authService.currentUserDisplayName ?: String.empty)
            } else {
                User(String.empty, "Anonymous")
            }
        }

    val isLoggedIn: Boolean
        get() = authService.currentUserId != null

    val loginState: Flow<Boolean>
        get() = callbackFlow {
            val cancellable = authService.observeAuthState { userId -> trySend(userId != null) }
            awaitClose { cancellable.cancel() }
        }

    val hasLoggedInOnce: Boolean
        get() = appSettings.hasLoggedIn

    @Throws(AuthException::class, CancellationException::class)
    suspend fun login(email: String, password: String) {
        when (val result = authService.signIn(email, password)) {
            is AuthResult.Success -> eventManager.logEvent(AnalyticsEvent.Login)
            is AuthResult.Failure -> {
                eventManager.logEvent(AnalyticsEvent.FailedLogin(reason = result.error.name))
                throw AuthException(result.error)
            }
        }
    }

    suspend fun refreshAuthentication() {
        if (!authService.refreshToken()) {
            authService.signOut()
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

        val result = try {
            authService.register(email, password)
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            eventManager.logException(ex, LoggableExceptionTypes.AUTHENTICATION, hashMapOf("action" to "register"))
            eventManager.logEvent(AnalyticsEvent.FailedRegistration(AuthErrors.UNKNOWN.name))
            throw AuthException(AuthErrors.UNKNOWN, ex)
        }

        when (result) {
            is AuthResult.Failure -> {
                eventManager.logEvent(AnalyticsEvent.FailedRegistration(result.error.name))
                throw AuthException(result.error)
            }
            is AuthResult.Success -> {
                authService.updateDisplayName(name)
                playerStorage.addPlayer(result.userId, PlayerDocument(name))
                appSettings.hasLoggedIn = true
                eventManager.setUserId(result.userId)
                eventManager.logEvent(AnalyticsEvent.CreateAccount)
            }
        }
    }
}
