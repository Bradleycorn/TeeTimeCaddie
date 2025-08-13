import Foundation
import TeeTimeCaddieKit


@MainActor
class TeetimeEntryViewModel: ObservableObject {
    private let teeTimesRepo: TeeTimesRepository
    private let authRepo: AuthRepository
    private let eventManager: EventManager
    
    init(teeTimesRepo: TeeTimesRepository = TeeTimesModule.shared.teeTimesRepository(), authRepo: AuthRepository = AuthModule.shared.authRepository(), eventManager: EventManager = AppModule.shared.eventManager()) {
        self.teeTimesRepo = teeTimesRepo
        self.authRepo = authRepo
        self.eventManager = eventManager
    }
    
    @Published
    private(set) var showLoadingProgress: Bool = false
    
    @Published
    private(set) var teeTimeAdded: Bool = false
    
    @Published
    var displayError: TeeTimeError? = nil
    
    func createTeeTime(course: String, date: Date, onTeeTimeCreated: @escaping ()->Void) {
        Task {
            displayError = nil
            showLoadingProgress = true

            do {
                let user = authRepo.currentUser.id
                
                let dateParts = Calendar.current.dateComponents([.month, .day, .year], from: date)
                                                
                guard let year = dateParts.year, let month = dateParts.month, let day = dateParts.day else {
                    let error = TeeTimeError.dateError(date)
                    eventManager.logError(error)
                    throw error
                }
                
                let localDate = LocalDate(year: Int32(year), monthNumber: Int32(month), dayOfMonth: Int32(day))
                
                let _ = try? await teeTimesRepo.createTeeTime(createdBy: user, course: course, dateTime: localDate)

                onTeeTimeCreated()
            } catch {
                displayError = error as? TeeTimeError
            }
        }
    }
}
