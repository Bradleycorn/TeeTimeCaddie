//
//  EditTeeTimeViewModel.swift
//  TeeTimeCaddie
//
//  Created by Claude on 2/6/26.
//

import Foundation
import TeeTimeCaddieKit
import Factory

@Observable
class EditTeeTimeViewModel {
    private let teeTimesRepo: TeeTimesRepository
    private let eventManager: EventManager
    private let teeTimeId: String

    private(set) var isLoading: Bool = true
    private(set) var showLoadingProgress: Bool = false
    private(set) var saveSuccess: Bool = false

    var courseName: String = ""
    var selectedDate: Date = Date()

    /// List of tee time slots, sorted by time
    private(set) var timeSlots: [TeeTimeSlot] = []

    /// The original tee time being edited
    private var originalTeeTime: TeeTime?

    /// Indicates whether any changes have been made
    var hasChanges: Bool {
        guard let original = originalTeeTime else { return false }
        let originalDate = original.date.toDate()
        let sortedOriginalTimes = original.times.sorted { $0.time.compareTo(other: $1.time) < 0 }
        let sortedCurrentTimes = timeSlots.sorted { $0.time.compareTo(other: $1.time) < 0 }

        return courseName != original.course ||
               !Calendar.current.isDate(selectedDate, inSameDayAs: originalDate) ||
               !areTimeSlotsEqual(sortedCurrentTimes, sortedOriginalTimes)
    }

    /// Indicates whether the Save button should be enabled
    var canSave: Bool {
        !courseName.isEmpty && !timeSlots.isEmpty && hasChanges
    }

    init(
        teeTimeId: String,
        teeTimesRepo: TeeTimesRepository = TeeTimesModule.shared.teeTimesRepository(),
        eventManager: EventManager = AppModule.shared.eventManager()
    ) {
        self.teeTimeId = teeTimeId
        self.teeTimesRepo = teeTimesRepo
        self.eventManager = eventManager
    }

    /// Loads the tee time from the repository.
    func loadTeeTime() async {
        isLoading = true
        defer { isLoading = false }

        do {
            if let teeTime = try await teeTimesRepo.getTeeTime(teeTimeId: teeTimeId) {
                originalTeeTime = teeTime
                courseName = teeTime.course
                selectedDate = teeTime.date.toDate()
                timeSlots = Array(teeTime.times)
            }
        } catch {
            print("Error loading tee time: \(error)")
        }
    }

    /// Logs the analytics event when the user clicks the "Add Time" button.
    func onAddTimeClick() {
        eventManager.logEvent(event: AnalyticsEvent.AddTimeClick())
    }

    /// Adds a new time slot with the default number of players (4).
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
        eventManager.logEvent(event: AnalyticsEvent.AddTime())
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
        eventManager.logEvent(event: AnalyticsEvent.NumberOfPlayersClick(numberOfPlayers: Int32(clampedPlayers)))
    }

    /// Removes a time slot from the list and logs the analytics event.
    /// - Parameter time: The LocalTime of the slot to remove.
    func removeTimeSlot(time: LocalTime) {
        eventManager.logEvent(event: AnalyticsEvent.RemoveTimeClick())
        timeSlots.removeAll { $0.time == time }
    }

    /// Saves the edited tee time.
    func saveTeeTime() {
        guard let original = originalTeeTime,
              !courseName.isEmpty,
              !timeSlots.isEmpty else {
            return
        }

        Task {
            showLoadingProgress = true
            defer { showLoadingProgress = false }

            do {
                let localDate = selectedDate.toLocalDate()

                let updatedTeeTime = TeeTime(
                    id: original.id,
                    createdBy: original.createdBy,
                    course: courseName,
                    date: localDate,
                    times: timeSlots
                )

                _ = try await teeTimesRepo.updateTeeTime(teeTime: updatedTeeTime)
                saveSuccess = true
            } catch {
                print("Error updating tee time: \(error)")
            }
        }
    }

    // MARK: - Private Helpers

    private func areTimeSlotsEqual(_ lhs: [TeeTimeSlot], _ rhs: [TeeTimeSlot]) -> Bool {
        guard lhs.count == rhs.count else { return false }
        for (left, right) in zip(lhs, rhs) {
            if left.time != right.time || left.numberOfPlayers != right.numberOfPlayers {
                return false
            }
        }
        return true
    }
}

// MARK: - LocalDate Extension

private extension LocalDate {
    func toDate() -> Date {
        let calendar = Calendar.current
        let components = DateComponents(
            year: Int(self.year),
            month: Int(self.month.number),
            day: Int(self.day)
        )
        return calendar.date(from: components) ?? Date()
    }
}
