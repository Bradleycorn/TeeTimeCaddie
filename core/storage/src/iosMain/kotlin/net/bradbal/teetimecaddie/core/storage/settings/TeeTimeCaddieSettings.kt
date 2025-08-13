package net.bradbal.teetimecaddie.core.storage.settings

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import platform.Foundation.NSUserDefaults

actual class TeeTimeCaddieSettings() {
    internal actual val settings: ObservableSettings = NSUserDefaultsSettings(NSUserDefaults(TeeTimeCaddieSettings::class.qualifiedName))
}