//
//  EditTeeTimeViewModel.swift
//  TeeTimeCaddie
//

import Foundation
import TeeTimeCaddieKit

@Observable
class EditTeeTimeViewModel {
    private let teeTimeId: String
    private let teeTimesRepo: TeeTimesRepository
    private let eventManager: EventManager

    // UI state
    private(set) var isLoading: Bool = true
    private(set) var showLoadingProgress: Bool = false
    private(set) var saveSuccess: Bool = false
    private(set) var loadError: Bool = false

    // Original tee time data (for change detection)
    private var originalTeeTime: TeeTime?

    // Editable form state
    private(set) var courseName: String = ""
    private(set) var date: Date = Date()
    private(set) var timeSlots: [TeeTimeSlot] = []

    /// Whether the form has been modified from the original tee time.
    var hasChanges: Bool {
        guard let original = originalTeeTime else { return false }
        let currentLocalDate = date.toLocalDate()
        let sortedCurrent = timeSlots.sorted { $0.time.compareTo(other: $1.time) < 0 }
        let sortedOriginal = original.times.sorted { $0.time.compareTo(other: $1.time) < 0 }

        return courseName != original.course ||
               currentLocalDate != original.date ||
               !areSlotsEqual(sortedCurrent, sortedOriginal)
    }

    private func areSlotsEqual(_ slots1: [TeeTimeSlot], _ slots2: [TeeTimeSlot]) -> Bool {
        guard slots1.count == slots2.count else { return false }
        for (slot1, slot2) in zip(slots1, slots2) {
            if slot1.time != slot2.time || slot1.numberOfPlayers != slot2.numberOfPlayers {
                return false
            }
        }
        return true
    }

    init(
        teeTimeId: String,
        teeTimesRepo: TeeTimesRepository = TeeTimesModule.shared.teeTimesRepository(),
        eventManager: EventManager = AppModule.shared.eventManager()
    ) {
        self.teeTimeId = teeTimeId
        self.teeTimesRepo = teeTimesRepo
        self.eventManager = eventManager

        Task {
            await loadTeeTime()
        }
    }

    @MainActor
    private func loadTeeTime() async {
        isLoading = true
        loadError = false

        do {
            if let teeTime = try await teeTimesRepo.getTeeTime(id: teeTimeId) {
                originalTeeTime = teeTime
                courseName = teeTime.course
                date = teeTime.date.toDate()
                timeSlots = Array(teeTime.times)
            } else {
                loadError = true
            }
        } catch {
            loadError = true
        }

        isLoading = false
    }

    /// Updates the course name.
    func updateCourseName(_ name: String) {
        courseName = name
    }

    /// Updates the date.
    func updateDate(_ newDate: Date) {
        date = newDate
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

    /// Saves the tee time with all changes.
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
        showLoadingProgress = true
        defer { showLoadingProgress = false }

        do {
            let localDate = date.toLocalDate()
            let updatedTeeTime = TeeTime(
                id: teeTimeId,
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
