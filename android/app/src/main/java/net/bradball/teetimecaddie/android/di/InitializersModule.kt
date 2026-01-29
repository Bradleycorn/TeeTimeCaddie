package net.bradball.teetimecaddie.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import net.bradball.teetimecaddie.android.analytics.FirebaseInitializer
import net.bradball.teetimecaddie.android.initializers.AppInitializer

/**
 * Dagger module that registers all [AppInitializer] implementations into the initialization system.
 *
 * This module uses Dagger's multibinding feature to collect all initializers into a single
 * `Set<AppInitializer>` that gets injected into [AppInitializers]. Each initializer is bound
 * using the [@Binds] and [@IntoSet] annotations to add it to the set.
 *
 * ## Adding New Initializers
 *
 * To register a new initializer:
 * 1. Create a class implementing [AppInitializer]
 * 2. Add a [@Binds] [@IntoSet] method in this module
 * 3. The initializer will automatically be discovered and executed
 *
 * ```kotlin
 * @Binds
 * @IntoSet
 * abstract fun provideMyInitializer(bind: MyInitializer): AppInitializer
 * ```
 *
 * ## Module Installation
 *
 * This module is installed in [SingletonComponent], ensuring initializers are created once
 * per app process and available throughout the application lifecycle.
 *
 * ## Execution Order
 *
 * The order of bindings in this module does not affect execution order. Execution order is
 * determined by:
 * - [InitializerPriority] of each initializer
 * - Dependencies declared in [AppInitializer.dependencies]
 *
 * ## Disabling Initializers
 *
 * To temporarily disable an initializer, comment out its binding method (as shown with the
 * commented examples). This is useful for testing or gradual rollout of new initialization logic.
 *
 * @see AppInitializer The interface implemented by all initializers
 * @see AppInitializers The orchestrator that consumes the initializer set
 */
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
