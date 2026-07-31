package net.bradbal.teetimecaddie.core.storage.settings

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set





//expect class SettingsFactory {
//    val factory: Settings.Factory
//
//    fun getObservableSettings(name: String): AppSettings
//    fun getEncryptedSettings(name: String): Settings
//}
//
//interface AppSettings {
//    fun get(key: String, defaultValue: String): String
//    fun set(key: String, value: String)
//
//    fun get(key: String, defaultValue: Boolean): Boolean
//    fun set(key: String, value: Boolean)
//
//    fun get(key: String, defaultValue: Int): Int
//    fun set(key: String, value: Int)
//
//    fun get(key: String, defaultValue: Long): Long
//    fun set(key: String, value: Long)
//
//    fun get(key: String, defaultValue: Double): Double
//    fun set(key: String, value: Double)
//
//    fun get(key: String, defaultValue: Float): Float
//    fun set(key: String, value: Float)
//}
//
//class KmpSettings(private val settings: ObservableSettings): AppSettings {
//    override fun get(key: String, defaultValue: String): String = settings.get(key, defaultValue)
//    override fun get(key: String, defaultValue: Boolean): Boolean = settings.get(key, defaultValue)
//    override fun get(key: String, defaultValue: Int): Int = settings.get(key, defaultValue)
//    override fun get(key: String, defaultValue: Long): Long = settings.get(key, defaultValue)
//    override fun get(key: String, defaultValue: Double): Double = settings.get(key, defaultValue)
//    override fun get(key: String, defaultValue: Float): Float = settings.get(key, defaultValue)
//
//    override fun set(key: String, value: String) { settings[key] = value }
//    override fun set(key: String, value: Boolean) { settings[key] = value }
//    override fun set(key: String, value: Int) { settings[key] = value }
//    override fun set(key: String, value: Long) { settings[key] = value }
//    override fun set(key: String, value: Double) { settings[key] = value }
//    override fun set(key: String, value: Float) { settings[key] = value }
//
//}