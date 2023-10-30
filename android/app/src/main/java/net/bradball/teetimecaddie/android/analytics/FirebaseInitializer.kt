package net.bradball.teetimecaddie.android.analytics

import android.app.Application
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import net.bradball.teetimecaddie.android.BuildConfig
import net.bradball.teetimecaddie.android.TeeTimeCaddieSdkInitializer
import net.bradball.teetimecaddie.android.initializers.AppInitializer
import net.bradball.teetimecaddie.android.initializers.InitializerPriority
import net.bradball.teetimecaddie.features.auth.AuthRepository
import javax.inject.Inject
import kotlin.reflect.KClass

class FirebaseInitializer @Inject constructor(private val authRepo: AuthRepository): AppInitializer {
    override val priority = InitializerPriority.ON_START
    override val dependencies: List<KClass<out AppInitializer>> = listOf(TeeTimeCaddieSdkInitializer::class)

    override suspend fun init(application: Application) {
        authRepo.refreshAuthentication()
    }
}