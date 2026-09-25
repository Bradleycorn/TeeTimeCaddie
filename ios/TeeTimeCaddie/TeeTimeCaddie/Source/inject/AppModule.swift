import Foundation
import Factory
import TeeTimeCaddieKit
import Firebase

final class AppModule: SharedContainer {
    static let shared = AppModule()
    var manager = ContainerManager()
    
    var teeTimeCaddieSdk: Factory<TeeTimeCaddieSdk> {
        self {
            // The AppDelegate initializes the SDK before any view is built, so this only reads it.
            // The defensive re-initialize that used to live here existed because the companion was
            // @ThreadLocal, which made `instance` per-thread on Kotlin/Native — so a lookup off the
            // main thread could find nothing. That annotation is gone.
            TeeTimeCaddieSdk.companion.getInstance()
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
