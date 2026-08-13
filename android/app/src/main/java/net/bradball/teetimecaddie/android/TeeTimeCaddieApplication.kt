package net.bradball.teetimecaddie.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import net.bradball.teetimecaddie.TeeTimeCaddieSdk
import net.bradball.teetimecaddie.android.analytics.FirebaseErrorLogger
import net.bradball.teetimecaddie.android.analytics.FirebaseTransactionLogger
import net.bradball.teetimecaddie.android.auth.FirebaseAuthService
import net.bradball.teetimecaddie.android.storage.FirebaseFirestoreClient
import net.bradball.teetimecaddie.initialize

@HiltAndroidApp
class TeeTimeCaddieApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize the SDK right away, so that objects it provides can be
        // injected as the rest of the app starts up. The app provides the platform
        // Firebase-backed loggers; the shared SDK depends only on the logger interfaces.
        TeeTimeCaddieSdk.initialize(
            this,
            FirebaseFirestoreClient(BuildConfig.DEBUG),
            FirebaseAuthService(BuildConfig.DEBUG),
            FirebaseErrorLogger(),
            FirebaseTransactionLogger()
        )
    }
}