package net.bradball.teetimecaddie

import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook

fun setup() {
    enableCrashlytics()
    setCrashlyticsUnhandledExceptionHook()
}
