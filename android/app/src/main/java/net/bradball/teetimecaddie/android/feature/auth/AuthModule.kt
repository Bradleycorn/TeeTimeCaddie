package net.bradball.teetimecaddie.android.feature.auth

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.bradball.teetimecaddie.android.BuildConfig
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.features.auth.AuthRepository
import net.bradball.teetimecaddie.features.auth.AuthSettings
import net.bradball.teetimecaddie.features.auth.createSettings
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AuthModule {

    @Provides
    @Singleton
    fun provideAuthSettings(@ApplicationContext context: Context): AuthSettings {
        return createSettings(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(eventManager: EventManager, authSettings: AuthSettings): AuthRepository {
       return  AuthRepository(eventManager, authSettings, useEmulator = BuildConfig.DEBUG)
    }
}