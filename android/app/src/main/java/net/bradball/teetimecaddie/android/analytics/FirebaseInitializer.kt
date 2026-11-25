package net.bradball.teetimecaddie.android.analytics

import android.app.Application
import net.bradball.teetimecaddie.android.initializers.AppInitializer
import net.bradball.teetimecaddie.android.initializers.InitializerPriority
import net.bradball.teetimecaddie.session.SessionManager
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * Re-validates the session each time the app starts or returns to the foreground, so an account
 * that was disabled or deleted elsewhere is signed out promptly.
 *
 * Goes through [SessionManager.refreshSession], as iOS does on becoming active. That call returns
 * at once and does its work in the SDK's own scope, once the session has loaded, so this
 * initializer never holds up the splash screen.
 */
class FirebaseInitializer @Inject constructor(private val sessionManager: SessionManager) : AppInitializer {
    override val priority = InitializerPriority.ON_START
    override val dependencies: List<KClass<out AppInitializer>> = listOf()

    override suspend fun init(application: Application) {
        sessionManager.refreshSession()
    }
}
