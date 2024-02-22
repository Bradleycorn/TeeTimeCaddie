import Foundation
import TeeTimeCaddieKit

@MainActor
class TeetTimesViewmodel: ObservableObject {
    init(teeTimesRepository: TeeTimesRepository = TeeTimesModule.shared.teeTimesRepository()) {
        self.teeTimesRepo = teeTimesRepository
    }
    
    private let teeTimesRepo: TeeTimesRepository
    
    @Published
    private(set) var teeTimes: [TeeTime] = []
    
    
}
