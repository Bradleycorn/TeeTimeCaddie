package net.bradbal.teetimecaddie.core.storage

import net.bradbal.teetimecaddie.core.storage.settings.TeeTimeCaddieSettings

expect class StorageModule {
    fun provideSettings(): TeeTimeCaddieSettings
}


fun StorageModule.providePlayerStorage(): PlayerStorage = PlayerStorage()
