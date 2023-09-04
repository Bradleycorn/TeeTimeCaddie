package net.bradball.teetimecaddie.features.auth

import net.bradbal.teetimecaddie.core.storage.AppSettings

const val AUTH_SETTINGS_FILE_NAME = "auth_settings"


class AuthSettings(private val settings: AppSettings) {
    val debugPort = 9099

    var hasLoggedIn: Boolean
        get() = settings.get("logged_in", false)
        set(value) { settings.set("logged_in", value) }
}

expect val AuthSettings.debugHost: String
