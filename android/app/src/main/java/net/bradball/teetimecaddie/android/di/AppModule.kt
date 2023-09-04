package net.bradball.teetimecaddie.android.di

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.bradball.teetimecaddie.android.analytics.FirebaseEventPlugin
import net.bradball.teetimecaddie.core.analytics.EventManager

@InstallIn(SingletonComponent::class)
@Module
open class AppModule {

    @Provides
    open fun provideEventManager(): EventManager = EventManager().apply {
        registerPlugin(FirebaseEventPlugin())
    }

    @Provides
    open fun providesFirebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @Provides
    open fun providesFirebasePerformance(): FirebasePerformance = FirebasePerformance.getInstance()
}