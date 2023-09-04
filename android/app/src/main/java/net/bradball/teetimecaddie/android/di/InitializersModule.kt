package net.bradball.teetimecaddie.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import net.bradball.teetimecaddie.android.analytics.FirebaseInitializer
import net.bradball.teetimecaddie.android.initializers.AppInitializer

@InstallIn(SingletonComponent::class)
@Module
abstract class InitializersModule {
    @Binds
    @IntoSet
    abstract fun provideFirebaseInitializer(bind: FirebaseInitializer): AppInitializer

//    @Binds
//    @IntoSet
//    abstract fun provideAppsFlyerInitializer(bind: AppsFlyerInitializer): AppInitializer
//
//    @Binds
//    @IntoSet
//    abstract fun provideBrazeInitializer(bind: BrazeInitializer): AppInitializer
//
//    @Binds
//    @IntoSet
//    abstract fun provideTealiumInitializer(bind: TealiumInitializer): AppInitializer
//
//    @Binds
//    @IntoSet
//    abstract fun provideFeatureToggleInitializer(bind: FeatureTogglesInitializer): AppInitializer
//
//    @Binds
//    @IntoSet
//    abstract fun provideCoilInitializer(bind: CoilInitializer): AppInitializer
//
//    @Binds
//    @IntoSet
//    abstract fun provideRemoteConfigInitializer(bind: RemoteConfigInitializer): AppInitializer
}
