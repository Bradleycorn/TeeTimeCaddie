//
//  AddTeeTimeViewModel.swift
//  TeeTimeCaddie
//
//  Created by Bradley Ball on 1/10/26.
//

import Foundation
import TeeTimeCaddieKit
import Factory

@Observable
class AddTeeTimeViewModel {
    private let teeTimesRepo: TeeTimesRepository
    private let authRepo: AuthRepository

    private(set) var showLoadingProgress: Bool = false
    private(set) var saveSuccess: Bool = false

    init(
        teeTimesRepo: TeeTimesRepository = TeeTimesModule.shared.teeTimesRepository(),
        authRepo: AuthRepository = AuthModule.shared.authRepository()
    ) {
        self.teeTimesRepo = teeTimesRepo
        self.authRepo = authRepo
    }

    func saveTeeTime(courseName: String, selectedDate: Date, selectedTime: Date, numberOfPlayers: Int) {
        
        guard !courseName.isEmpty, numberOfPlayers > 0, selectedDate >= Date() else {
            return
        }
                    
        Task {
            showLoadingProgress = true
            defer { showLoadingProgress = false }

            do {
                let localDate = selectedDate.toLocalDate()
                let localTime = selectedTime.toLocalTime()

                _ = try await teeTimesRepo.createTeeTime(
                    createdBy: authRepo.currentUser.id,
                    course: courseName,
                    date: localDate,
                    time: localTime,
                    numberOfPlayers: Int32(numberOfPlayers)
                )

                saveSuccess = true
            } catch {
                // Per acceptance criteria, no error handling required
                print("Error saving tee time: \(error)")
            }
        }
    }
}

// MARK: - Date Conversion Extensions
private extension Date {
    func toLocalDate() -> LocalDate {
        let calendar = Calendar.current
        let components = calendar.dateComponents([.year, .month, .day], from: self)
        return LocalDate(
            year: Int32(components.year ?? 2024),
            month: Int32(components.month ?? 1),
            day: Int32(components.day ?? 1)
        )
    }

    func toLocalTime() -> LocalTime {
        let calendar = Calendar.current
        let components = calendar.dateComponents([.hour, .minute], from: self)
        return LocalTime(
            hour: Int32(components.hour ?? 0),
            minute: Int32(components.minute ?? 0),
            second: 0,
            nanosecond: 0
        )
    }
}
