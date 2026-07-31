package net.bradbal.teetimecaddie.core.storage.settings

import android.content.Context
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings

actual class TeeTimeCaddieSettings(context: Context) {
    internal actual val settings: ObservableSettings = SharedPreferencesSettings(
        delegate = context.getSharedPreferences(TeeTimeCaddieSettings::class.qualifiedName, Context.MODE_PRIVATE),
        commit = false
    )
}