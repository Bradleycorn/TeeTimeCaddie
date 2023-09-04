package net.bradball.teetimecaddie.features.auth

import net.bradbal.teetimecaddie.core.storage.SettingsFactory

fun createSettings(): AuthSettings {
    val settings = SettingsFactory()
    return AuthSettings(settings.getObservableSettings(AUTH_SETTINGS_FILE_NAME))
}