package net.bradball.teetimecaddie

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import net.bradbal.teetimecaddie.core.storage.StorageModule
import net.bradbal.teetimecaddie.core.storage.providePlayerPhotoStorage
import net.bradbal.teetimecaddie.core.storage.providePlayerStorage
import net.bradbal.teetimecaddie.core.storage.provideTeeTimeStorage
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.features.auth.AuthRepository
import net.bradball.teetimecaddie.features.auth.AuthRepositoryImpl
import net.bradball.teetimecaddie.features.players.PlayerRepository
import net.bradball.teetimecaddie.features.players.PlayerRepositoryImpl
import net.bradball.teetimecaddie.features.teetimes.TeeTimesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.bradball.teetimecaddie.session.SessionManager

class TeeTimeCaddieSdk private constructor(useLocalResources: Boolean, private val storageModule: StorageModule) {

    val eventManager: EventManager by lazy { EventManager.getInstance() }

    // Held as lazy vals rather than built per call, so there is exactly one of each for the life
    // of the SDK. That matters most for `sessionManager`: two instances would mean two independent
    // sessionState flows, and an app shell observing the wrong one would simply never update.
    // Making it structural here means a misconfigured DI in either app cannot reintroduce the bug.
    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(eventManager) }

    val playerRepository: PlayerRepository by lazy {
        PlayerRepositoryImpl(
            eventManager,
            storageModule.providePlayerStorage(),
            storageModule.providePlayerPhotoStorage()
        )
    }

    val teeTimesRepository: TeeTimesRepository by lazy {
        TeeTimesRepository(eventManager, storageModule.provideTeeTimeStorage())
    }

    val sessionManager: SessionManager by lazy {
        SessionManager(authRepository, playerRepository, eventManager, sdkScope)
    }

    /**
     * A scope for SDK work that has to outlive whatever asked for it.
     *
     * Created here because the SDK is already the thing with process lifetime, so both apps get the
     * same guarantee without either of them having to arrange it. [SupervisorJob] so one failed job
     * cannot cancel the scope and silently disable the rest.
     *
     * Not exposed: callers get the behaviour through the method that needs it
     * ([SessionManager.abandonSignUp]), not a scope to launch their own work in.
     */
    private val sdkScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        if (useLocalResources) {
            Firebase.auth.useEmulator(FirebaseConfig.debugHost, FirebaseConfig.authDebugPort)
            Firebase.firestore.useEmulator(FirebaseConfig.debugHost, FirebaseConfig.firestoreDebugPort)
            Firebase.firestore.setSettings(
                host = "${FirebaseConfig.debugHost}:${FirebaseConfig.firestoreDebugPort}",
                persistenceEnabled = false,
                sslEnabled = false 
            )
        }
    }

    // Deliberately NOT @ThreadLocal. On Kotlin/Native that annotation makes `instance` per-thread,
    // so getInstance() off the main thread would build a second SDK — with its own lazy
    // sessionManager and its own sessionState flow — defeating the singletons above. The iOS app
    // used to paper over that with a defensive re-initialize in its DI container.
    companion object {
        private var instance: TeeTimeCaddieSdk? = null

        val isInitialized: Boolean
            get() = instance != null

        fun getInstance(): TeeTimeCaddieSdk {
            return instance ?: throw IllegalStateException("TeeTimeCaddieSdk not initialized")
        }

        internal fun initialize(useLocalResources: Boolean, storageModule: StorageModule) {
            if (instance == null) {
                instance = TeeTimeCaddieSdk(useLocalResources, storageModule)
            }
        }
    }
}
