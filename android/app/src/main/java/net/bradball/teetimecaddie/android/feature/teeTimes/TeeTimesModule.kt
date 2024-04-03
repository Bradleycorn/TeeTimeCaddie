package net.bradball.teetimecaddie.android.feature.teeTimes

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.bradball.teetimecaddie.TeeTimeCaddieSdk
import net.bradball.teetimecaddie.features.auth.AuthRepository
import net.bradball.teetimecaddie.features.teetimes.TeeTimesRepository
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class TeeTimesModule {

    @Provides
    @Singleton
    fun provideTeeTimesRepository(): TeeTimesRepository {
        return TeeTimeCaddieSdk.getInstance().provideTeeTimesRepository()
    }
}