import Foundation
import Factory
import TeeTimeCaddieKit

final class AppModule: SharedContainer {
    static let shared = AppModule()
    var manager = ContainerManager()
    
    var eventManager: Factory<EventManager> {
        self {
            EventManager().also { e in
                e.registerPlugin(eventPlugin: FirebaseEventPlugin())
            }
        }.singleton
    }
    
    
    var fireabseCrashlytics: Factory<FirebaseCrashlytics> {
        self { Firebase.shared.crashlytics }
            .singleton
    }
    
    var firebasePerformance: Factory<FirebasePerformance> {
        self { Firebase.shared.performance }
            .singleton
    }
    
}
