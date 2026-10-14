import Foundation
import Factory
import TeeTimeCaddieKit

final class AuthModule: SharedContainer {
    static let shared = AuthModule()
    var manager = ContainerManager()
        
    var authRepository: Factory<AuthRepository> {
        self { AppModule.shared.teeTimeCaddieSdk().authRepository }
            .singleton
    }

    var playerRepository: Factory<PlayerRepository> {
        self { AppModule.shared.teeTimeCaddieSdk().playerRepository }
            .singleton
    }

    /// The SDK holds this as a lazy singleton, so `.singleton` here mirrors Hilt rather than being
    /// what guarantees a single `sessionState` flow.
    var sessionManager: Factory<SessionManager> {
        self { AppModule.shared.teeTimeCaddieSdk().sessionManager }
            .singleton
    }
     
}
