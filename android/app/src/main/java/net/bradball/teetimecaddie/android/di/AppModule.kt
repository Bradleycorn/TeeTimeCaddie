package net.bradball.teetimecaddie.android.di

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.bradball.teetimecaddie.TeeTimeCaddieSdk
import net.bradball.teetimecaddie.android.analytics.FirebaseEventPlugin
import net.bradball.teetimecaddie.core.analytics.EventManager
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
open class AppModule {

    /**
     * The SDK's own event manager, with the Firebase plugin attached.
     *
     * `@Singleton` is load-bearing: without it `@Provides` runs per injection, and since this
     * returns the *same* shared instance every time, `registerPlugin` would stack another
     * `FirebaseEventPlugin` on it each call — reporting every event more than once.
     */
    @Provides
    @Singleton
    open fun provideEventManager(): EventManager = TeeTimeCaddieSdk.getInstance().eventManager.apply {
        registerPlugin(FirebaseEventPlugin())
    }

    /**
     * A scope that outlives every screen. See [ApplicationScope] for when that is the right tool.
     *
     * [SupervisorJob] so one failed job cannot cancel the scope and quietly disable the rest.
     */
    @Provides
    @Singleton
    @ApplicationScope
    open fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    open fun providesFirebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @Provides
    open fun providesFirebasePerformance(): FirebasePerformance = FirebasePerformance.getInstance()
}