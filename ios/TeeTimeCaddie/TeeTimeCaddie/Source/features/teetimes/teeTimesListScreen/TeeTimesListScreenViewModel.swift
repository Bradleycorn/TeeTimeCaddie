import Foundation
import TeeTimeCaddieKit

@MainActor
class TeeTimesListScreenViewModel: ObservableObject {

    private let teeTimesRepo: TeeTimesRepository
    private let authRepo: AuthRepository

    init(teeTimesRepository: TeeTimesRepository = TeeTimesModule.shared.teeTimesRepository(),
         authRepository: AuthRepository = AuthModule.shared.authRepository()) {
        self.teeTimesRepo = teeTimesRepository
        self.authRepo = authRepository
    }
        
    @Published
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
    
}
