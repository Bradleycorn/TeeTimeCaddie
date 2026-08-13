import Foundation
import Factory
import TeeTimeCaddieKit
import Firebase

final class AppModule: SharedContainer {
    static let shared = AppModule()
    var manager = ContainerManager()
    
    var teeTimeCaddieSdk: Factory<TeeTimeCaddieSdk> {
        self {
            if (!TeeTimeCaddieSdk.companion.isInitialized) {
                TeeTimeCaddieSdk.companion.initialize(
                    firestoreClient: FirebaseFirestoreClient(useEmulator: IS_DEBUG_BUILD),
                    authService: FirebaseAuthService(useEmulator: IS_DEBUG_BUILD),
                    errorLogger: FirebaseErrorLogger(),
                    transactionLogger: FirebaseTransactionLogger()
                )
            }
            return TeeTimeCaddieSdk.companion.getInstance()
        }.singleton
    }

    var eventManager: Factory<EventManager> {
        self {
            // Use the single EventManager owned by the SDK (the one its repositories log to),
            // rather than a separate instance, and register the app's analytics plugin on it.
            self.teeTimeCaddieSdk().eventManager.also { e in
                e.registerPlugin(eventPlugin: FirebaseEventPlugin())
            }
        }.singleton
    }
    
    
    var fireabseCrashlytics: Factory<Crashlytics> {
        self { Crashlytics.crashlytics() }
            .singleton
    }
    
}
