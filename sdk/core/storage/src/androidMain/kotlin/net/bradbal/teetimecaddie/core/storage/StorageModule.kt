package net.bradbal.teetimecaddie.core.storage

import android.content.Context
import net.bradbal.teetimecaddie.core.storage.settings.TeeTimeCaddieSettings

actual class StorageModule(private val appContext: Context) {
    actual fun provideSettings(): TeeTimeCaddieSettings = TeeTimeCaddieSettings(appContext)
}