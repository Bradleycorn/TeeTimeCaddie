import Foundation
import TeeTimeCaddieKit

@Observable
class TeeTimesListScreenViewModel {

    private let teeTimesRepo: TeeTimesRepository
    private let authRepo: AuthRepository
    private let eventManager: EventManager

    init(teeTimesRepository: TeeTimesRepository = TeeTimesModule.shared.teeTimesRepository(),
         authRepository: AuthRepository = AuthModule.shared.authRepository(),
         eventManager: EventManager = AppModule.shared.eventManager()) {
        self.teeTimesRepo = teeTimesRepository
        self.authRepo = authRepository
        self.eventManager = eventManager
    }

}
