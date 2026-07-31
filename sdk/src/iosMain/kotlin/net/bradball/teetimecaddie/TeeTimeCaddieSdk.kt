package net.bradball.teetimecaddie

import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook
import net.bradbal.teetimecaddie.core.storage.StorageModule

fun TeeTimeCaddieSdk.Companion.initialize(useLocalResources: Boolean) {
    initialize(useLocalResources, StorageModule())
    setCrashlyticsUnhandledExceptionHook()
}