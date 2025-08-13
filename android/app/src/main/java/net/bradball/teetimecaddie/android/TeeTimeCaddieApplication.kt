package net.bradball.teetimecaddie.android

import android.app.Application
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.HiltAndroidApp
import net.bradball.teetimecaddie.TeeTimeCaddieSdk
import net.bradball.teetimecaddie.initialize
import javax.inject.Inject

@HiltAndroidApp
class TeeTimeCaddieApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize the SDK right away, so that objects it provides can be
        // injected as the rest of the app starts up.
        TeeTimeCaddieSdk.initialize(this, false) //BuildConfig.DEBUG)
    }
}