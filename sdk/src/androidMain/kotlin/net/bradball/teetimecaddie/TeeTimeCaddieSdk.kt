package net.bradball.teetimecaddie

import android.content.Context
import net.bradbal.teetimecaddie.core.storage.StorageModule
import net.bradball.teetimecaddie.core.analytics.ErrorLogger
import net.bradball.teetimecaddie.core.analytics.TransactionLogger
import net.bradball.teetimecaddie.core.models.storage.FirestoreClient
import net.bradball.teetimecaddie.features.auth.AuthService

fun TeeTimeCaddieSdk.Companion.initialize(
    context: Context,
    firestoreClient: FirestoreClient,
    authService: AuthService,
    errorLogger: ErrorLogger,
    transactionLogger: TransactionLogger
) {
    initialize(StorageModule(context.applicationContext), firestoreClient, authService, errorLogger, transactionLogger)
}