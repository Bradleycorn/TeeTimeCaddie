//
//  EditTeeTimeViewModel.swift
//  TeeTimeCaddie
//

import Foundation
import TeeTimeCaddieKit

@Observable
class EditTeeTimeViewModel {
    private let teeTimesRepo: TeeTimesRepository
    private let authRepo: AuthRepository
    private let eventManager: EventManager
    private let teeTimeId: String

    private(set) var isLoading: Bool = true
    private(set) var showSaveProgress: Bool = false
    private(set) var saveSuccess: Bool = false

    /// Original tee time data for change detection
    private var originalTeeTime: TeeTime?

    /// Editable fields
    private(set) var courseName: String = ""
    private(set) var selectedDate: Date = Date()

    /// List of tee time slots, sorted by time
    private(set) var timeSlots: [TeeTimeSlot] = []

    /// Whether the form has been modified from its original state.
    var hasChanges: Bool {
        guard let original = originalTeeTime else { return false }

        let originalDate = original.date.toDate()
        let currentSlotsSorted = timeSlots.sorted { $0.time.compareTo(other: $1.time) < 0 }
        let originalSlotsSorted = original.times.sorted { $0.time.compareTo(other: $1.time) < 0 }

        return courseName != original.course ||
            !Calendar.current.isDate(selectedDate, inSameDayAs: originalDate) ||
            currentSlotsSorted != originalSlotsSorted
    }

    init(
        teeTimeId: String,
        teeTimesRepo: TeeTimesRepository = TeeTimesModule.shared.teeTimesRepository(),
        authRepo: AuthRepository = AuthModule.shared.authRepository(),
        eventManager: EventManager = AppModule.shared.eventManager()
    ) {
        self.teeTimeId = teeTimeId
        self.teeTimesRepo = teeTimesRepo
        self.authRepo = authRepo
        self.eventManager = eventManager

        Task {
            await loadTeeTime()
        }
    }

    @MainActor
    private func loadTeeTime() async {
        isLoading = true
        defer { isLoading = false }

        do {
            // Get the first emission from the flow
            for try await teeTimes in teeTimesRepo.getTeeTimes(player: authRepo.currentUser.id) {
                if let teeTime = teeTimes.first(where: { $0.id == teeTimeId }) {
                    originalTeeTime = teeTime
                    courseName = teeTime.course
                    selectedDate = teeTime.date.toDate()
                    timeSlots = Array(teeTime.times)
                }
                break // Only need the first emission
            }
        } catch {
            print("Error loading tee time: \(error)")
        }
    }

    /// Updates the course name.
    func updateCourseName(_ name: String) {
        courseName = name
    }

    /// Updates the selected date.
    func updateDate(_ date: Date) {
        selectedDate = date
    }

    /// Logs the analytics event when the user clicks the "Add Time" button.
    func onAddTimeClick() {
        eventManager.logEvent(event: AnalyticsEvent.AddTimeClick())
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

    /// Saves the updated tee time.
    func saveTeeTime() {
        guard !courseName.isEmpty, !timeSlots.isEmpty, let original = originalTeeTime else {
            return
        }

        Task {
            await performSave(original: original)
        }
    }

    @MainActor
    private func performSave(original: TeeTime) async {
        showSaveProgress = true
        defer { showSaveProgress = false }

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
            print("Error saving tee time: \(error)")
        }
    }
}
