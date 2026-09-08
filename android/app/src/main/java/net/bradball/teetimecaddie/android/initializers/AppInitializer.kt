package net.bradball.teetimecaddie.android.initializers

import android.app.Application
import kotlin.reflect.KClass

/**
 * Interface for defining application initialization tasks that run during app startup.
 *
 * AppInitializer provides a structured way to organize and manage initialization logic
 * across different lifecycle stages. Each initializer can declare when it should run,
 * what dependencies it has, and perform its initialization work asynchronously.
 *
 * ## Lifecycle Integration
 *
 * Initializers are executed at specific points in the application lifecycle based on
 * their [priority]:
 * - **APP_LAUNCH**: Runs immediately when the app starts, before any UI is shown
 * - **ON_CREATE**: Runs after process lifecycle onCreate, suitable for heavy initialization
 * - **ON_START**: Runs when app comes to foreground, can execute multiple times
 *
 * ## Dependency Management
 *
 * Initializers can declare dependencies on other initializers via [dependencies]. The
 * AppInitializers system guarantees:
 * - Dependencies are initialized before the dependent initializer runs
 * - Circular dependencies are detected and result in [InitializationException]
 * - Each initializer runs only once per app session (except ON_START priority)
 *
 * ## Implementation Example
 *
 * ```kotlin
 * class FirebaseInitializer @Inject constructor(
 *     private val firebaseApp: FirebaseApp
 * ) : AppInitializer {
 *     override val priority = InitializerPriority.APP_LAUNCH
 *     override val dependencies = emptyList<KClass<out AppInitializer>>()
 *
 *     override suspend fun init(application: Application) {
 *         Firebase.initialize(application)
 *         FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
 *     }
 * }
 * ```
 *
 * ## Registration
 *
 * Initializers must be bound in [InitializersModule] using Dagger's multibinding:
 * ```kotlin
 * @Binds
 * @IntoSet
 * abstract fun provideFirebaseInitializer(bind: FirebaseInitializer): AppInitializer
 * ```
 *
 * ## Error Handling
 *
 * If [init] throws an exception, it will be wrapped in [InitializationException] and:
 * - Prevent subsequent initializers from running in the same priority level
 * - Set the initialization state to Failed
 * - Be available for logging/monitoring via the exception's [LoggableException] interface
 *
 * @see AppInitializers The orchestrator that manages initializer execution
 * @see InitializerPriority Priority levels that determine when initialization occurs
 * @see InitializationState State tracking for initialization progress
 */
interface AppInitializer {
    /**
     * Defines when this initializer should run during the application lifecycle.
     *
     * @see InitializerPriority for available priority levels and their execution timing
     */
    val priority: InitializerPriority

    /**
     * List of initializer classes that must complete before this initializer runs.
     *
     * Dependencies are resolved recursively and executed in dependency-first order.
     * Circular dependencies will cause an [InitializationException] to be thrown.
     *
     * Example:
     * ```kotlin
     * override val dependencies = listOf(
     *     FirebaseInitializer::class,
     *     RemoteConfigInitializer::class
     * )
     * ```
     *
     * @return List of [KClass] references to required initializers
     */
    val dependencies: List<KClass<out AppInitializer>>

    /**
     * Performs the initialization work for this component.
     *
     * This method is called exactly once per app session (unless [priority] is [InitializerPriority.ON_START]),
     * after all [dependencies] have completed successfully. The method runs on a coroutine
     * context managed by the AppInitializers system.
     *
     * ## Thread Safety
     * This method may be called from a background thread. If you need to perform work on
     * the main thread, use appropriate coroutine dispatchers.
     *
     * ## Exception Handling
     * Any exception thrown from this method will be caught and wrapped in [InitializationException],
     * which will halt the initialization sequence for the current priority level.
     *
     * @param application The application instance, never null
     * @throws Exception if initialization fails. Will be wrapped in [InitializationException]
     */
    suspend fun init(application: Application)
}

fun Set<AppInitializer>.filterBy(priority: InitializerPriority): Set<AppInitializer> = filter {
    it.priority == priority
}.toSet()
