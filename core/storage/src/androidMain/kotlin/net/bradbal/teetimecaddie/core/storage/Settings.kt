package net.bradbal.teetimecaddie.core.storage

import android.content.Context
import android.content.SharedPreferences
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

//actual class SettingsFactory(private val context: Context) {
//   actual val factory: Settings.Factory = SharedPreferencesSettings.Factory(context)
//
//   actual fun getObservableSettings(name: String): AppSettings {
//      val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
//      return KmpSettings(SharedPreferencesSettings(prefs, false))
//   }
//
//   actual fun getEncryptedSettings(name: String): Settings {
//      TODO("Encrypted settings on Android not implemented yet.")
//   }
//
//}
