package net.bradball.teetimecaddie

import co.touchlab.crashkios.crashlytics.enableCrashlytics
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import net.bradbal.teetimecaddie.core.storage.StorageModule
import net.bradbal.teetimecaddie.core.storage.providePlayerStorage
import net.bradbal.teetimecaddie.core.storage.provideTeeTimeStorage
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.features.auth.AuthRepository
import net.bradball.teetimecaddie.features.teetimes.TeeTimesRepository
import kotlin.native.concurrent.ThreadLocal

class TeeTimeCaddieSdk private constructor(useLocalResources: Boolean, private val storageModule: StorageModule) {

    val eventManager: EventManager by lazy { EventManager() }

    fun provideAuthRepository(): AuthRepository = AuthRepository(eventManager, storageModule.provideSettings(), storageModule.providePlayerStorage())

    fun provideTeeTimesRepository(): TeeTimesRepository = TeeTimesRepository(eventManager, storageModule.provideTeeTimeStorage())

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

    @ThreadLocal
    companion object {
        private var instance: TeeTimeCaddieSdk? = null

        fun getInstance(): TeeTimeCaddieSdk {
            return instance ?: throw IllegalStateException("TeeTimeCaddieSdk not initialized")
        }

        internal fun initialize(useLocalResources: Boolean, storageModule: StorageModule) {
            instance = TeeTimeCaddieSdk(useLocalResources, storageModule)
            enableCrashlytics()
        }
    }
}

