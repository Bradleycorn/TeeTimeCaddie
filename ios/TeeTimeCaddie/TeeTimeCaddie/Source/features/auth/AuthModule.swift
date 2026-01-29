import Foundation
import Factory
import TeeTimeCaddieKit

final class AuthModule: SharedContainer {
    static let shared = AuthModule()
    var manager = ContainerManager()
        
    var authRepository: Factory<AuthRepository> {
        self { AppModule.shared.teeTimeCaddieSdk().provideAuthRepository() }
            .singleton
    }
     
}
