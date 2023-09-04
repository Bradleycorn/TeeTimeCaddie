import Foundation
import Factory
import TeeTimeCaddieKit

final class AuthModule: SharedContainer {
    static let shared = AuthModule()
    var manager = ContainerManager()
    
    var authSettings: Factory<AuthSettings> {
        self { AuthModuleKt.createSettings() }
    }
    
    var authRepository: Factory<AuthRepository> {
        self { AuthRepository(eventManager: AppModule.shared.eventManager(), authSettings: self.authSettings(), useEmulator: IS_DEBUG_BUILD)}
            .singleton
    }
     
}
