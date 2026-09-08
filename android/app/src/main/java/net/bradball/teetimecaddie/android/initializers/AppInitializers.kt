package net.bradball.teetimecaddie.android.initializers

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import net.bradball.teetimecaddie.core.analytics.LoggableException
import net.bradball.teetimecaddie.core.analytics.LoggableExceptionTypes
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

class InitializationException(
    val initializer: AppInitializer,
    message: String? = null,
    cause: Throwable? = null): IllegalStateException(message, cause), LoggableException {
    override val logInfo: HashMap<String, Any?> = hashMapOf()
    override val loggableType: LoggableExceptionTypes = LoggableExceptionTypes.STARTUP
}


/**
 * Orchestrates the execution of [AppInitializer] instances during application startup, both
 * cold starts and when returning from the background.
 *
 * AppInitializers is the central component of the initialization system, responsible for
 * managing the lifecycle, dependency resolution, and execution order of all registered
 * initializers. It integrates with Android's [ProcessLifecycleOwner] to trigger initialization
 * at appropriate points in the application lifecycle.
 *
 * ## System Overview
 *
 * The AppInitializers system provides a structured approach to organizing startup tasks:
 * - **Priority-based execution**: Initializers run in groups based on [InitializerPriority]
 * - **Dependency management**: Automatic resolution and ordering of initializer dependencies
 * - **State tracking**: Observable state flows for monitoring initialization progress
 * - **Error isolation**: Failures are contained to prevent cascading issues
 *
 * ## Lifecycle Integration
 *
 * AppInitializers hooks into the Android process lifecycle to execute initializers at three
 * distinct phases:
 *
 * 1. **APP_LAUNCH**: Triggered in the init block, before onCreate
 * 2. **ON_CREATE**: Triggered when ProcessLifecycleOwner reaches onCreate
 * 3. **ON_START**: Triggered each time ProcessLifecycleOwner reaches onStart
 *
 * Each phase waits for the previous phase to complete successfully before executing.
 *
 * ## Dependency Resolution
 *
 * Initializers declare dependencies via [AppInitializer.dependencies]. The system:
 * - Resolves dependencies recursively
 * - Executes dependencies before dependents
 * - Detects and prevents circular dependencies
 * - Ensures each initializer runs only once per session (except ON_START)
 *
 * ```kotlin
 * // RemoteConfigInitializer will run before AnalyticsInitializer
 * class AnalyticsInitializer : AppInitializer {
 *     override val dependencies = listOf(RemoteConfigInitializer::class)
 *     // ...
 * }
 * ```
 *
 * ## State Monitoring
 *
 * The system exposes state through Flow properties:
 * - [initState]: Combined state of APP_LAUNCH and ON_CREATE phases
 * - [state]: Combined state of all three phases including ON_START
 *
 * ```kotlin
 * class MainActivity : ComponentActivity() {
 *     @Inject lateinit var appInitializers: AppInitializers
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         lifecycleScope.launch {
 *             appInitializers.initState.collect { state ->
 *                 when (state) {
 *                     is InitializationState.Pending -> showSplashScreen()
 *                     is InitializationState.Complete -> showMainContent()
 *                     is InitializationState.Failed -> showErrorScreen(state.error)
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * ## Error Handling
 *
 * When an initializer throws an exception:
 * - The exception is wrapped in [InitializationException]
 * - The current priority level's state changes to [InitializationState.Failed]
 * - Subsequent initializers in that priority level are skipped
 * - The error is available for logging via [LoggableException] interface
 *
 * ## Registration
 *
 * Initializers are registered via Dagger's multibinding in [InitializersModule]:
 *
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class InitializersModule {
 *     @Binds
 *     @IntoSet
 *     abstract fun provideFirebaseInitializer(bind: FirebaseInitializer): AppInitializer
 * }
 * ```
 *
 * ## Thread Safety
 *
 * All initialization work runs on coroutines managed by the ProcessLifecycleOwner's
 * lifecycleScope. Initializers can safely perform blocking operations or switch dispatchers
 * as needed within their [AppInitializer.init] implementations.
 *
 * @property application The application instance passed to initializers
 * @property initializers Set of all registered initializers, provided via Dagger multibinding
 * @property initState Flow combining APP_LAUNCH and ON_CREATE states for splash screen management
 * @property state Flow combining all three priority level states for full initialization tracking
 *
 * @see AppInitializer The interface implemented by all initializers
 * @see InitializerPriority Priority levels that determine execution timing
 * @see InitializationState State tracking for initialization progress
 * @see InitializersModule Dagger module for registering initializers
 */
@Singleton
class AppInitializers @Inject constructor(
    private val application: Application,
    private val initializers: Set<@JvmSuppressWildcards AppInitializer>
): DefaultLifecycleObserver {

    /**
     * Set of initializer classes that have completed execution.
     *
     * Used to prevent duplicate execution of initializers. ON_START priority initializers
     * are removed from this set on each onStart event to allow re-initialization.
     */
    private var completedInitializers: MutableList<KClass<out AppInitializer>> = mutableListOf()

    /**
     * Job for APP_LAUNCH priority initializers.
     *
     * Stored to allow ON_CREATE phase to wait for launch completion.
     */
    private var launchJob: Job? = null

    /**
     * Job for ON_CREATE priority initializers.
     *
     * Stored to allow ON_START phase to wait for create completion.
     */
    private var createJob: Job? = null

    /**
     * State flow for APP_LAUNCH priority initializers.
     */
    private val launchState = MutableStateFlow<InitializationState>(InitializationState.Pending)

    /**
     * State flow for ON_CREATE priority initializers.
     */
    private val createState = MutableStateFlow<InitializationState>(InitializationState.Pending)

    /**
     * State flow for ON_START priority initializers.
     */
    private val startState = MutableStateFlow<InitializationState>(InitializationState.Pending)

    /**
     * Combined state of APP_LAUNCH and ON_CREATE initializers.
     *
     * This flow is useful for splash screen management, as it becomes [InitializationState.Complete]
     * once the critical startup phases finish. ON_START initializers don't block this state since
     * they run in the background after the app is already interactive.
     *
     * Emits:
     * - [InitializationState.Pending] while either phase is running
     * - [InitializationState.Complete] when both phases complete successfully
     * - [InitializationState.Failed] if either phase fails
     */
    val initState = combine(launchState, createState) { launch, create ->
        when {
            launch is InitializationState.Failed -> launch
            create is InitializationState.Failed -> create
            launch == InitializationState.Complete
                    && create == InitializationState.Complete -> InitializationState.Complete
            else -> InitializationState.Pending
        }
    }

    /**
     * Combined state of all initialization phases.
     *
     * This flow tracks the complete initialization lifecycle including ON_START initializers.
     * Use this for comprehensive initialization monitoring or when ON_START initializers
     * must complete before certain features are enabled.
     *
     * Emits:
     * - [InitializationState.Pending] while any phase is running
     * - [InitializationState.Complete] when all phases complete successfully
     * - [InitializationState.Failed] if any phase fails
     */
    val state = combine(launchState, createState, startState) { launch, create, start ->
        when {
            launch is InitializationState.Failed -> launch
            create is InitializationState.Failed -> create
            start is InitializationState.Failed -> start
            launch == InitializationState.Complete
                    && create == InitializationState.Complete
                    && start == InitializationState.Complete -> InitializationState.Complete
            else -> InitializationState.Pending
        }
    }

    fun log(msg: String) {
        Log.d("INIT_DEBUG", msg)
    }

    /**
     * Initializes the AppInitializers system and triggers APP_LAUNCH priority initializers.
     *
     * This init block executes when the AppInitializers instance is created (typically during
     * Dagger graph construction). It:
     * - Launches a coroutine in ProcessLifecycleOwner's scope to run APP_LAUNCH initializers
     * - Registers this class as a lifecycle observer to handle ON_CREATE and ON_START phases
     * - Updates [launchState] based on initialization success or failure
     *
     * APP_LAUNCH initializers run before any UI is shown, making this phase suitable for
     * critical initialization like crash reporting setup.
     */
    init {
        val process = ProcessLifecycleOwner.get()
        launchJob = process.lifecycleScope.launch {
            launchState.value = InitializationState.Pending
            try {
                log("Running launch initializers")
                init(initializers.filterBy(InitializerPriority.APP_LAUNCH))
                launchState.value = InitializationState.Complete
                log("Launch initializers complete")
            } catch (ex: InitializationException) {
                log("Launch initializers failed: ${ex.message}")
                launchState.value = InitializationState.Failed(ex)
            }
        }
        process.lifecycle.addObserver(this)
    }

    /**
     * Handles the onCreate lifecycle event and triggers ON_CREATE priority initializers.
     *
     * This method executes when the process lifecycle reaches onCreate, which occurs after
     * the app's main activity is created but before it becomes visible. It:
     * - Waits for APP_LAUNCH initializers to complete via [launchJob]
     * - Executes ON_CREATE priority initializers if the launch phase succeeded
     * - Updates [createState] based on initialization success or failure
     *
     * ON_CREATE initializers run after the initial UI is shown, making this phase suitable
     * for heavy initialization work like analytics setup or feature flag systems.
     *
     * @param owner The ProcessLifecycleOwner that triggered this callback
     */
    override fun onCreate(owner: LifecycleOwner) {
        log("OnCreate initializers triggered. Waiting for launch initializers to finish.")
        super.onCreate(owner)
        createJob = owner.lifecycleScope.launch {
            createState.value = InitializationState.Pending
            launchJob?.join()
            log("================")
            log("Running OnCreate initializers")
            if (launchState.value == InitializationState.Complete) {
                try {
                    init(initializers.filterBy(InitializerPriority.ON_CREATE))
                    createState.value = InitializationState.Complete
                    log("OnCreate initializers complete")
                } catch (ex: InitializationException) {
                    log("OnCreate initializers failed: ${ex.message}")
                    createState.value = InitializationState.Failed(ex)
                }
            }
        }
    }

    /**
     * Handles the onStart lifecycle event and triggers ON_START priority initializers.
     *
     * This method executes each time the app enters the foreground (onStart lifecycle event).
     * Unlike APP_LAUNCH and ON_CREATE, this phase runs multiple times throughout the app session.
     * It:
     * - Waits for ON_CREATE initializers to complete via [createJob]
     * - Removes ON_START initializers from [completedInitializers] to allow re-execution
     * - Executes ON_START priority initializers if the create phase succeeded
     * - Updates [startState] based on initialization success or failure
     *
     * ON_START initializers are suitable for work that should refresh when returning from
     * background, such as session tracking, token refresh, or dynamic configuration updates.
     *
     * If no ON_START initializers are registered, this method immediately sets [startState]
     * to Complete and returns.
     *
     * @param owner The ProcessLifecycleOwner that triggered this callback
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        log("===============")
        val startInitializers = initializers.filterBy(InitializerPriority.ON_START)
        if (startInitializers.isEmpty()) {
            log("No OnStart Initializers defined. Skipping OnStart routine")
            startState.value = InitializationState.Complete
            return
        }

        log("OnStart initializers triggered. Waiting for OnCreate initializers to finish.")
        owner.lifecycleScope.launch {
            startState.value = InitializationState.Pending
            createJob?.join()
            log("Running OnStart initializers")
            if (createState.value == InitializationState.Complete) {
                try {
                    // remove the "on start" initializers from the completed list, so that
                    // they will get re-initialized.
                    completedInitializers.removeAll(startInitializers.map { it::class })
                    init(startInitializers)
                    startState.value = InitializationState.Complete
                    log("OnStart initializers Complete")
                } catch (ex: InitializationException) {
                    log("OnStart initializers failed: ${ex.message}")
                    startState.value = InitializationState.Failed(ex)
                }
            }
        }
    }

    /**
     * Executes a set of initializers in sequence, respecting dependencies.
     *
     * This internal method iterates through the provided initializers and executes each one
     * via [runInitializer]. The [runInitializer] method handles dependency resolution, so
     * initializers may execute in a different order than the iteration order to satisfy
     * dependency requirements.
     *
     * Each initializer is executed exactly once during this call, unless it was already
     * completed (tracked via [completedInitializers]).
     *
     * @param initializers Set of initializers to execute, typically filtered by priority
     * @throws InitializationException if any initializer or its dependencies fail
     */
    private suspend fun init(initializers: Set<AppInitializer>) {
        log("${initializers.size} initializers to run..")
        initializers.forEach {
            runInitializer(it, mutableSetOf())
        }
    }

    /**
     * Executes a single initializer and its dependencies.
     *
     * This method handles dependency resolution and circular dependency detection. It tracks
     * currently initializing instances to detect cycles and uses [completedInitializers] to
     * prevent duplicate execution.
     *
     * @param initializer The initializer to execute
     * @param initializing Set of initializers currently being initialized, used for cycle detection
     * @throws InitializationException if a circular dependency is detected or if initialization fails
     */
    private suspend fun runInitializer(initializer: AppInitializer, initializing: MutableSet<AppInitializer>) {
        if (initializing.contains(initializer)) {
            throw InitializationException(
                initializer,
                "Cannot initialize ${initializer::class.simpleName}. Circular dependency detected.")
        }
        if (!completedInitializers.contains(initializer::class)) {
            log("Running ${initializer::class.simpleName}")
            initializing.add(initializer)
            try {
                log("Checking dependencies for ${initializer::class.simpleName}")
                processDependencies(initializer.dependencies, initializing)
                initializer.init(application)
                initializing.remove(initializer)
                completedInitializers.add(initializer::class)
                log("Finished ${initializer::class.simpleName}")
            } catch (ex: Throwable) {
                throw InitializationException(initializer, cause = ex)
            }
        } else {
            log("Skipping ${initializer::class.simpleName}. It is already initialized.")
        }
    }

    /**
     * Recursively processes and executes initializer dependencies.
     *
     * For each dependency class, finds the corresponding initializer instance and executes it
     * via [runInitializer]. Throws an exception if a dependency cannot be resolved.
     *
     * @param dependencies List of initializer classes that must be executed first
     * @param initializing Set of initializers currently being initialized, passed to [runInitializer]
     * @throws Exception if a dependency cannot be found in the registered initializers set
     * @throws InitializationException if dependency initialization fails
     */
    private suspend fun processDependencies(dependencies: List<KClass<out AppInitializer>>, initializing: MutableSet<AppInitializer>) {
        dependencies.forEach {  dep ->
            log("${dep.simpleName} is a dependency. Initializing it.")
            val initializer = initializers.find { it::class == dep }
            initializer?.let { runInitializer(it, initializing) } ?:
                throw Exception("Error resolving dependency. Could not find the ${dep.simpleName} initializer.")
        }
    }

}

