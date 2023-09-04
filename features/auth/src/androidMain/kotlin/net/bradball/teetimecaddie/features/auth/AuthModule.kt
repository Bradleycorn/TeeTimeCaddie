package net.bradball.teetimecaddie.features.auth

import android.content.Context
import net.bradbal.teetimecaddie.core.storage.SettingsFactory

fun createSettings(context: Context): AuthSettings {
    val settings = SettingsFactory(context)
    return AuthSettings(settings.getObservableSettings(AUTH_SETTINGS_FILE_NAME))
}