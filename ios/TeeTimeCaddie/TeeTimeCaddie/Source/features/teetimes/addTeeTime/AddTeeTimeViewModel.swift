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

    /// List of tee time slots, sorted by time
    private(set) var timeSlots: [TeeTimeSlot] = []

    init(
        teeTimesRepo: TeeTimesRepository = TeeTimesModule.shared.teeTimesRepository(),
        authRepo: AuthRepository = AuthModule.shared.authRepository()
    ) {
        self.teeTimesRepo = teeTimesRepo
        self.authRepo = authRepo
    }

    /// Adds a new time slot with the default number of players (4).
    /// If the time already exists in the list, it will not be added.
    /// - Parameter time: The time to add (as a Date).
    /// - Returns: true if the time was added, false if it already existed.
    @discardableResult
    func addTimeSlot(time: Date) -> Bool {
        let localTime = time.toLocalTime()

        // Check if time already exists
        if timeSlots.contains(where: { $0.time == localTime }) {
            return false
        }

        let newSlot = TeeTimeSlot(time: localTime, numberOfPlayers: 4)
        timeSlots.append(newSlot)
        timeSlots.sort { $0.time.compareTo(other: $1.time) < 0 }
        return true
    }

    /// Updates the number of players for a specific time slot.
    /// - Parameters:
    ///   - time: The LocalTime of the slot to update.
    ///   - numberOfPlayers: The new number of players (1-4).
    func updatePlayerCount(time: LocalTime, numberOfPlayers: Int) {
        guard let index = timeSlots.firstIndex(where: { $0.time == time }) else { return }
        let clampedPlayers = min(max(numberOfPlayers, 1), 4)
        timeSlots[index] = TeeTimeSlot(time: time, numberOfPlayers: Int32(clampedPlayers))
    }

    /// Saves the tee time with all added time slots.
    /// - Parameters:
    ///   - courseName: The name of the golf course.
    ///   - selectedDate: The date of the tee time.
    func saveTeeTime(courseName: String, selectedDate: Date) {
        guard !courseName.isEmpty, !timeSlots.isEmpty else {
            return
        }

        Task {
            showLoadingProgress = true
            defer { showLoadingProgress = false }

            do {
                let localDate = selectedDate.toLocalDate()

                _ = try await teeTimesRepo.createTeeTime(
                    createdBy: authRepo.currentUser.id,
                    course: courseName,
                    date: localDate,
                    times: timeSlots
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
extension Date {
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

