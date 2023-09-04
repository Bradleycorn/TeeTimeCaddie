package net.bradbal.teetimecaddie.core.storage

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual class SettingsFactory {
    actual val factory: Settings.Factory = NSUserDefaultsSettings.Factory()

    actual fun getObservableSettings(name: String): AppSettings {
        return KmpSettings(NSUserDefaultsSettings(NSUserDefaults(name)))
    }

    actual fun getEncryptedSettings(name: String): Settings {
        TODO("Keychain settings on iOS not implemented yet.")
    }

}