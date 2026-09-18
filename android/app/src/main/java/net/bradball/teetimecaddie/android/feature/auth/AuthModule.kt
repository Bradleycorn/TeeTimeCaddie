package net.bradball.teetimecaddie.android.feature.auth

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.bradball.teetimecaddie.TeeTimeCaddieSdk
import net.bradball.teetimecaddie.features.auth.AuthRepository
import net.bradball.teetimecaddie.features.players.PlayerRepository
import net.bradball.teetimecaddie.session.SessionManager
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
       return TeeTimeCaddieSdk.getInstance().provideAuthRepository()
    }

    @Provides
    @Singleton
    fun providePlayerRepository(): PlayerRepository {
        return TeeTimeCaddieSdk.getInstance().providePlayerRepository()
    }

    /**
     * The SDK already holds this as a lazy singleton, so `@Singleton` here is belt-and-braces
     * rather than the thing that guarantees one `sessionState` flow.
     */
    @Provides
    @Singleton
    fun provideSessionManager(): SessionManager {
        return TeeTimeCaddieSdk.getInstance().sessionManager
    }
}