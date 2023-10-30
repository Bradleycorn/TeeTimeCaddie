package net.bradball.teetimecaddie.android.feature.auth

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.bradball.teetimecaddie.TeeTimeCaddieSdk
import net.bradball.teetimecaddie.features.auth.AuthRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
       return TeeTimeCaddieSdk.getInstance().provideAuthRepository()
    }
}