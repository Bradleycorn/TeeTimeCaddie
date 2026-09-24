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
                TeeTimeCaddieSdk.companion.initialize(useLocalResources: IS_DEBUG_BUILD)
            }
            return TeeTimeCaddieSdk.companion.getInstance()
        }.singleton
    }
    
    var eventManager: Factory<EventManager> {
        self {
            // The SDK's own instance, not a new one. Every repository and SessionManager logs
            // through `sdk.eventManager`; building a second manager here left that one with no
            // plugins, so repository-level events and errors were silently dropped on iOS.
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
