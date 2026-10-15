//
//  NewGameViewModel.swift
//  TeeTimeCaddie
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

/// State of the New game form.
///
/// A struct rather than an enum, because its parts are independent: the confirmation opens over a
/// filled-in form, and a failed publish leaves the form as it was. Free of SDK types so
/// ``NewGameContent`` previews are pure construction; the display text is formatted by the
/// ViewModel when a value is chosen.
struct NewGameUiState: Equatable {
    var course: String = ""
    var date: Date?
    var dateText: String?
    var time: Date?
    var timeText: String?
    var slots: Int = NewGameUiState.maxSlots

    /// Whether the "Publish with no invitees?" alert is up. Until invitees arrive (TTC-69) every
    /// publish asks.
    var showNoInviteeConfirm: Bool = false
    var isSubmitting: Bool = false

    /// The SDK's `MAX_TEE_TIME_SLOTS`, repeated as a literal so building this state — in a preview,
    /// say — never reaches into the Kotlin framework. ``NewGameViewModelTests`` pins the two together.
    static let maxSlots = 4

    /// A course of only spaces doesn't count. Slots never block publishing; they start at 4.
    var canSubmit: Bool {
        !course.trimmingCharacters(in: .whitespaces).isEmpty
            && date != nil
            && time != nil
            && !isSubmitting
    }
}

/// The Swift twin of `NewGameViewModel.kt` — same state, same method names.
@MainActor
@Observable
final class NewGameViewModel {

    private(set) var uiState = NewGameUiState()

    private let gamesRepository: GamesRepository
    private let currentPlayer: () -> Player?
    private let toastPresenter: TtcToastPresenter

    init(
        gamesRepository: GamesRepository = GamesModule.shared.gamesRepository(),
        currentPlayer: @escaping () -> Player? = GamesModule.shared.currentPlayer(),
        toastPresenter: TtcToastPresenter = AppModule.shared.toastPresenter()
    ) {
        self.gamesRepository = gamesRepository
        self.currentPlayer = currentPlayer
        self.toastPresenter = toastPresenter
    }

    func onCourseChange(_ course: String) {
        uiState.course = course
    }

    func onDateSelected(_ date: Date) {
        uiState.date = date
        uiState.dateText = date.toLocalDate().formattedDate
    }

    func onTimeSelected(_ time: Date) {
        uiState.time = time
        uiState.timeText = time.toLocalTime().formattedTime
    }

    func onSlotsChange(_ slots: Int) {
        uiState.slots = slots
    }

    /// Publish was tapped: ask first, since nobody has been invited.
    func onPublishClick() {
        guard uiState.canSubmit else { return }
        uiState.showNoInviteeConfirm = true
    }

    /// "Keep editing": close the alert and leave the form exactly as it was.
    func onKeepEditing() {
        uiState.showNoInviteeConfirm = false
    }

    /// "Publish anyway".
    ///
    /// - Parameter onPublished: Called once the game is saved, to leave the form.
    func onPublishAnyway(onPublished: () -> Void) async {
        uiState.showNoInviteeConfirm = false

        let state = uiState
        guard state.canSubmit,
              let organizer = currentPlayer(),
              let date = state.date,
              let time = state.time else { return }

        uiState.isSubmitting = true

        do {
            let result = try await gamesRepository.publishGame(
                organizer: organizer,
                courseName: state.course,
                date: date.toLocalDate(),
                time: time.toLocalTime(),
                slots: Int32(state.slots)
            )

            switch onEnum(of: result) {
            case .success(let success):
                // The stored spelling, which may differ from what was typed.
                let course = success.data.course.name
                toastPresenter.show(GMR.strings().new_game_published_message.localized(course))
                onPublished()
            case .failure(let failure):
                report(failure.error)
            }
        } catch {
            report(error.asTeeTimeCaddieException())
        }
    }

    private func report(_ error: any TeeTimeCaddieException) {
        toastPresenter.show(error.displayMessage.localized(args: error.messageArgs))
        uiState.isSubmitting = false
    }
}
