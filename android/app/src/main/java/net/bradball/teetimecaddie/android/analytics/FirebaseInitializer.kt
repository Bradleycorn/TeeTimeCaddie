package net.bradball.teetimecaddie.android.analytics

import android.app.Application
import kotlinx.coroutines.delay
import net.bradball.teetimecaddie.android.initializers.AppInitializer
import net.bradball.teetimecaddie.android.initializers.InitializerPriority
import net.bradball.teetimecaddie.features.auth.AuthRepository
import javax.inject.Inject
import kotlin.reflect.KClass

class FirebaseInitializer @Inject constructor(private val authRepo: AuthRepository): AppInitializer {
    override val priority = InitializerPriority.ON_START
    override val dependencies: List<KClass<out AppInitializer>> = listOf()

    override suspend fun init(application: Application) {
        delay(3_000)
        authRepo.refreshAuthentication()
    }
}