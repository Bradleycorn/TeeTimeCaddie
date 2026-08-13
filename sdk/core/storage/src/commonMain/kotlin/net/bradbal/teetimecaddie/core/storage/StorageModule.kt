package net.bradbal.teetimecaddie.core.storage

import net.bradball.teetimecaddie.core.models.storage.FirestoreClient
import net.bradbal.teetimecaddie.core.storage.settings.TeeTimeCaddieSettings

expect class StorageModule {
    fun provideSettings(): TeeTimeCaddieSettings
}


fun StorageModule.providePlayerStorage(firestore: FirestoreClient): PlayerStorage = PlayerStorage(firestore)

fun StorageModule.provideTeeTimeStorage(firestore: FirestoreClient): TeeTimeStorage = TeeTimeStorage(firestore)