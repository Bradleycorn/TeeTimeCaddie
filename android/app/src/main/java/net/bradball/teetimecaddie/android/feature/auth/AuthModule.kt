package net.bradball.teetimecaddie.android.feature.auth

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.bradball.teetimecaddie.TeeTimeCaddieSdk
import net.bradball.teetimecaddie.features.auth.AuthRepository
import net.bradball.teetimecaddie.features.players.PlayerRepository
import net.bradball.teetimecaddie.session.SessionManager
import kotlinx.coroutines.flow.StateFlow
import net.bradball.teetimecaddie.session.SessionState
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
       return TeeTimeCaddieSdk.getInstance().authRepository
    }

    @Provides
    @Singleton
    fun providePlayerRepository(): PlayerRepository {
        return TeeTimeCaddieSdk.getInstance().playerRepository
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

    /**
     * The session, on its own, for ViewModels that only need to know who is signed in.
     *
     * A seam rather than a convenience: `SessionManager`'s constructor is internal to the SDK, so a
     * ViewModel that takes it cannot be unit tested. One that takes this flow can be handed a
     * `MutableStateFlow(SessionState.SignedIn(previewPlayer))`.
     */
    @Provides
    fun provideSessionState(sessionManager: SessionManager): StateFlow<@JvmSuppressWildcards SessionState> {
        return sessionManager.sessionState
    }
}
