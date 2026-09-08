package net.bradbal.teetimecaddie.core.storage

import net.bradbal.teetimecaddie.core.storage.settings.TeeTimeCaddieSettings

actual class StorageModule {
    actual fun provideSettings(): TeeTimeCaddieSettings = TeeTimeCaddieSettings()
}