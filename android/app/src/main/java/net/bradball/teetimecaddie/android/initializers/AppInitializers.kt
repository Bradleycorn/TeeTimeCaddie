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


@Singleton
class AppInitializers @Inject constructor(
    private val application: Application,
    private val initializers: Set<@JvmSuppressWildcards AppInitializer>
): DefaultLifecycleObserver {

    private var completedInitializers: MutableList<KClass<out AppInitializer>> = mutableListOf()

    private var launchJob: Job? = null
    private var createJob: Job? = null

    private val launchState = MutableStateFlow<InitializationState>(InitializationState.Pending)
    private val createState = MutableStateFlow<InitializationState>(InitializationState.Pending)
    private val startState = MutableStateFlow<InitializationState>(InitializationState.Pending)

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

    init {
        launchJob = ProcessLifecycleOwner.get().lifecycleScope.launch {
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
    }

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

    private suspend fun init(initializers: Set<AppInitializer>) {
        log("${initializers.size} initializers to run..")
        initializers.forEach {
            runInitializer(it, mutableSetOf())
        }
    }

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

    private suspend fun processDependencies(dependencies: List<KClass<out AppInitializer>>, initializing: MutableSet<AppInitializer>) {
        dependencies.forEach {  dep ->
            log("${dep.simpleName} is a dependency. Initializing it.")
            val initializer = initializers.find { it::class == dep }
            initializer?.let { runInitializer(it, initializing) } ?:
                throw Exception("Error resolving dependency. Could not find the ${dep.simpleName} initializer.")
        }
    }

}

