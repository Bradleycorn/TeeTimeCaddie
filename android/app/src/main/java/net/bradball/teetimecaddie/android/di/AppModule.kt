package net.bradball.teetimecaddie.android.di

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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

    @Provides
    open fun providesFirebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @Provides
    open fun providesFirebasePerformance(): FirebasePerformance = FirebasePerformance.getInstance()
}