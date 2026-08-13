package net.bradball.teetimecaddie

import net.bradbal.teetimecaddie.core.storage.StorageModule
import net.bradbal.teetimecaddie.core.storage.providePlayerStorage
import net.bradbal.teetimecaddie.core.storage.provideTeeTimeStorage
import net.bradball.teetimecaddie.core.analytics.ErrorLogger
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.analytics.TransactionLogger
import net.bradball.teetimecaddie.core.models.storage.FirestoreClient
import net.bradball.teetimecaddie.features.auth.AuthRepository
import net.bradball.teetimecaddie.features.auth.AuthService
import net.bradball.teetimecaddie.features.teetimes.TeeTimesRepository
import kotlin.native.concurrent.ThreadLocal

class TeeTimeCaddieSdk private constructor(
    private val storageModule: StorageModule,
    private val firestoreClient: FirestoreClient,
    private val authService: AuthService,
    errorLogger: ErrorLogger,
    transactionLogger: TransactionLogger
) {

    val eventManager: EventManager by lazy { EventManager(errorLogger, transactionLogger) }

    fun provideAuthRepository(): AuthRepository = AuthRepository(eventManager, storageModule.provideSettings(), storageModule.providePlayerStorage(firestoreClient), authService)

    fun provideTeeTimesRepository(): TeeTimesRepository = TeeTimesRepository(eventManager, storageModule.provideTeeTimeStorage(firestoreClient))

    @ThreadLocal
    companion object {
        private var instance: TeeTimeCaddieSdk? = null

        val isInitialized: Boolean
            get() = instance != null

        fun getInstance(): TeeTimeCaddieSdk {
            return instance ?: throw IllegalStateException("TeeTimeCaddieSdk not initialized")
        }

        internal fun initialize(
            storageModule: StorageModule,
            firestoreClient: FirestoreClient,
            authService: AuthService,
            errorLogger: ErrorLogger,
            transactionLogger: TransactionLogger
        ) {
            if (instance == null) {
                instance = TeeTimeCaddieSdk(storageModule, firestoreClient, authService, errorLogger, transactionLogger)
            }
        }
    }
}

