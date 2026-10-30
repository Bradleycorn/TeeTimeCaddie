//
//  NewGameViewModel.swift
//  TeeTimeCaddie
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

/// The Swift twin of `NewGameViewModel.kt` — same state, same method names.
@MainActor
@Observable
final class NewGameViewModel {

    private(set) var uiState: NewGameUiState = NewGameUiState.companion.defaultValue

    private let gamesRepository: GamesRepository
    private let currentPlayer: () -> Player?
    private let toastPresenter: TtcToastPresenter
    private let eventManager: EventManager

    init(
        gamesRepository: GamesRepository = GamesModule.shared.gamesRepository(),
        currentPlayer: @escaping () -> Player? = GamesModule.shared.currentPlayer(),
        toastPresenter: TtcToastPresenter = AppModule.shared.toastPresenter(),
        eventManager: EventManager = AppModule.shared.eventManager()
    ) {
        self.gamesRepository = gamesRepository
        self.currentPlayer = currentPlayer
        self.toastPresenter = toastPresenter
        self.eventManager = eventManager
    }

    func onCourseChange(_ course: String) {
        uiState = uiState.withCourse(course: course)
    }

    func onDateSelected(_ date: Date) {
        uiState = uiState.withDate(date: date.toLocalDate())
    }

    func onTimeSelected(_ time: Date) {
        uiState = uiState.withTime(time: time.toLocalTime())
    }

    func onSlotsChange(_ slots: Int) {
        eventManager.logEvent(event: AnalyticsEvent.PlayerSlotsClick(playerCount: Int32(slots)))
        uiState = uiState.withSlots(slots: Int32(slots))
    }

    /// Publish was tapped: ask first, since nobody has been invited.
    func onPublishClick() {
        guard uiState.canSubmit else { return }
        uiState = uiState.withNoInviteeConfirm(showNoInviteeConfirm: true)
    }

    /// "Keep editing": close the alert and leave the form exactly as it was.
    func onKeepEditing() {
        eventManager.logEvent(event: AnalyticsEvent.NoInvites(response: .keepEditing))
        onDismissNoInviteeConfirm()
    }

    /// The alert closed. Every way it closes ends here, buttons included, so it logs nothing itself.
    func onDismissNoInviteeConfirm() {
        uiState = uiState.withNoInviteeConfirm(showNoInviteeConfirm: false)
    }

    /// "Publish anyway".
    ///
    /// - Parameter onPublished: Called once the game is saved, to leave the form.
    func onPublishAnyway(onPublished: () -> Void) async {
        eventManager.logEvent(event: AnalyticsEvent.NoInvites(response: .publishAnyway))
        uiState = uiState.withNoInviteeConfirm(showNoInviteeConfirm: false)

        let state = uiState
        guard state.canSubmit,
              let organizer = currentPlayer(),
              let date = state.date,
              let time = state.time else { return }

        uiState = uiState.withSubmitting(isSubmitting: true)

        do {
            let result = try await gamesRepository.publishGame(
                organizer: organizer,
                courseName: state.course,
                date: date,
                time: time,
                slots: state.slots
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
        uiState = uiState.withSubmitting(isSubmitting: false)
    }
}
