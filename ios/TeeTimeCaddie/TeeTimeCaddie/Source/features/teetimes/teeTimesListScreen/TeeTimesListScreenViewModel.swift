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

    private(set) var uiState: UiState<[TeeTime]> = .Loading

    var addButtonEnabled: Bool {
        uiState != .Loading
    }

    func loadTeetimes() async {

        if !uiState.hasContent {
            uiState = .Loading
        }

        for await teeTimesList in teeTimesRepo.getTeeTimes(player: authRepo.currentUser.id) {
            print("Got TeeTimes \(teeTimesList.count)")
            uiState = (teeTimesList.isEmpty) ? .Empty : .Content(teeTimesList)
        }
    }

    func onTeeTimeClick() {
        eventManager.logEvent(event: AnalyticsEvent.TeeTimeListItemClick())
    }
}
