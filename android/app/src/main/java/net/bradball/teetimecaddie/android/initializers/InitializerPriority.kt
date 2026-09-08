package net.bradball.teetimecaddie.android.initializers

/**
 * Defines the execution priority and lifecycle timing for [AppInitializer] instances.
 *
 * The priority determines when during the application lifecycle an initializer will execute.
 * Initializers are grouped by priority and executed sequentially within each group, with
 * dependency resolution ensuring proper ordering.
 *
 * ## Execution Order
 *
 * Priorities execute in the following sequence:
 * 1. **APP_LAUNCH** - Before any UI is shown
 * 2. **ON_CREATE** - After process lifecycle onCreate
 * 3. **ON_START** - When app enters foreground (repeatable)
 *
 * Within each priority level, initializers execute in dependency-first order based on
 * their [AppInitializer.dependencies] declarations.
 *
 * ## Priority Selection Guidelines
 *
 * - **APP_LAUNCH**: Use for critical initialization that must complete before the app
 *   becomes interactive (e.g., crash reporting, essential SDK initialization)
 *
 * - **ON_CREATE**: Use for heavy initialization that can be deferred until after the
 *   splash screen (e.g., analytics setup, feature flag systems)
 *
 * - **ON_START**: Use for initialization that should refresh when the app returns to
 *   foreground (e.g., session tracking, token refresh)
 *
 * ## Example
 *
 * ```kotlin
 * class CrashReportingInitializer @Inject constructor() : AppInitializer {
 *     // Critical for catching early crashes
 *     override val priority = InitializerPriority.APP_LAUNCH
 *     override val dependencies = emptyList<KClass<out AppInitializer>>()
 *
 *     override suspend fun init(application: Application) {
 *         FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
 *     }
 * }
 *
 * class AnalyticsInitializer @Inject constructor() : AppInitializer {
 *     // Can wait until after splash screen
 *     override val priority = InitializerPriority.ON_CREATE
 *     override val dependencies = listOf(CrashReportingInitializer::class)
 *
 *     override suspend fun init(application: Application) {
 *         FirebaseAnalytics.getInstance(application)
 *     }
 * }
 * ```
 *
 * @see AppInitializer.priority for setting an initializer's priority
 * @see AppInitializers for the orchestration system that respects priorities
 */
enum class InitializerPriority {

    /**
     * Executes immediately when [AppInitializers] is created, before any UI is displayed.
     *
     * Use this priority for critical initialization that must complete before the app
     * becomes interactive. Examples:
     * - Crash reporting setup
     * - Security initialization
     * - Essential SDK initialization
     *
     * Runs only once per app process.
     */
    APP_LAUNCH,

    /**
     * Executes after the process lifecycle reaches onCreate state.
     *
     * Use this priority for heavy initialization work that can be deferred until after
     * the initial UI is shown. Examples:
     * - Analytics initialization
     * - Feature flag systems
     * - Non-critical third-party SDKs
     *
     * Runs only once per app process.
     */    
    ON_CREATE,

    /**
     * Executes each time the app enters the foreground (onStart lifecycle event).
     *
     * Use this priority for initialization that should refresh when the app returns
     * from background. Examples:
     * - Session tracking
     * - Authentication token refresh
     * - Dynamic configuration updates
     *
     * Unlike other priorities, this executes multiple times throughout the app session.
     */
    ON_START
}