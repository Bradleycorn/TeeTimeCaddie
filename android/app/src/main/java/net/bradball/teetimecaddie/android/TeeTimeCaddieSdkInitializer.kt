package net.bradball.teetimecaddie.android

import android.app.Application
import net.bradball.teetimecaddie.TeeTimeCaddieSdk
import net.bradball.teetimecaddie.android.initializers.AppInitializer
import net.bradball.teetimecaddie.android.initializers.InitializerPriority
import net.bradball.teetimecaddie.initialize
import javax.inject.Inject
import kotlin.reflect.KClass

class TeeTimeCaddieSdkInitializer @Inject constructor(): AppInitializer {
    override val priority: InitializerPriority = InitializerPriority.APP_LAUNCH
    override val dependencies: List<KClass<out AppInitializer>> = listOf()

    override suspend fun init(application: Application) {
        TeeTimeCaddieSdk.initialize(application, useLocalResources = BuildConfig.DEBUG)
    }
}