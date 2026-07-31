package net.bradbal.teetimecaddie.core.storage.settings

import com.russhwolf.settings.get
import com.russhwolf.settings.set

var TeeTimeCaddieSettings.hasLoggedIn: Boolean
    get() = settings["logged_in"] ?: false
    set(value) { settings["logged_in"] = value }
