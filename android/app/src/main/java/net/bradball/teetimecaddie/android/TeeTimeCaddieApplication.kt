package net.bradball.teetimecaddie.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import net.bradball.teetimecaddie.TeeTimeCaddieSdk
import net.bradball.teetimecaddie.initialize

@HiltAndroidApp
class TeeTimeCaddieApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize the SDK right away, so that objects it provides can be
        // injected as the rest of the app starts up.
        TeeTimeCaddieSdk.initialize(this, BuildConfig.DEBUG)
    }
}