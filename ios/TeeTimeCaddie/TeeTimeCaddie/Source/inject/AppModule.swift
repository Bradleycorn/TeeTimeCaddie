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
            EventManager().also { e in
                e.registerPlugin(eventPlugin: FirebaseEventPlugin())
            }
        }.singleton
    }
    
    
    var fireabseCrashlytics: Factory<Crashlytics> {
        self { Crashlytics.crashlytics() }
            .singleton
    }
    
}
