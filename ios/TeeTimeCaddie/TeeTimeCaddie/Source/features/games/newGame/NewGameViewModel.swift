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
    private let contactsRepositoryFactory: (any DeviceContactsReader) -> any ContactsRepository
    private let currentPlayer: () -> Player?
    private let toastPresenter: TtcToastPresenter
    private let eventManager: EventManager

    init(
        gamesRepository: GamesRepository = GamesModule.shared.gamesRepository(),
        contactsRepositoryFactory: @escaping (any DeviceContactsReader) -> any ContactsRepository =
            GamesModule.shared.contactsRepositoryFactory(),
        currentPlayer: @escaping () -> Player? = GamesModule.shared.currentPlayer(),
        toastPresenter: TtcToastPresenter = AppModule.shared.toastPresenter(),
        eventManager: EventManager = AppModule.shared.eventManager()
    ) {
        self.gamesRepository = gamesRepository
        self.contactsRepositoryFactory = contactsRepositoryFactory
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

    /// "Add from contacts" was tapped: the system contact picker opens. No permission is needed.
    ///
    /// - Parameter pickContacts: Opens the picker, which reports how it closed to
    ///   ``onContactPickerDismissed(_:)``.
    func onAddFromContactsClick(pickContacts: () -> Void) {
        eventManager.logEvent(event: AnalyticsEvent.AddFromContactsClick.shared)
        pickContacts()
    }

    /// The system picker closed. `picked` reads the contacts it returned, or is nil when nothing was
    /// picked. Picked contacts join the end of the list, sorted by name among themselves; anyone
    /// already invited at that number isn't added twice. The picker can't show who's already
    /// invited, so it only ever adds; the ✕ on a row removes.
    func onContactPickerDismissed(_ picked: (any DeviceContactsReader)?) async {
        eventManager.logEvent(event: AnalyticsEvent.ContactPickerDismiss(selection: picked == nil ? .none : .some))
        guard let picked else { return }
        let repository = contactsRepositoryFactory(picked)
        do {
            let result = try await repository.addPickedContacts(invitees: uiState.invitees)
            switch onEnum(of: result) {
            case .success(let success):
                if let invitees = success.data as? [NewInvitee] {
                    uiState = uiState.withInvitees(invitees: invitees)
                }
            case .failure(let failure):
                show(failure.error)
            }
        } catch {
            show(error.asTeeTimeCaddieException())
        }
    }

    /// An invitee's ✕ was tapped. They're removed at once, without asking.
    func onRemoveInvitee(_ invitee: NewInvitee) {
        eventManager.logEvent(event: AnalyticsEvent.RemoveInviteeClick.shared)
        uiState = uiState.withInvitees(invitees: uiState.invitees.filter { $0 != invitee })
    }

    /// Publish was tapped. With nobody invited, ask first; otherwise publish.
    ///
    /// - Parameters:
    ///   - confirmNoInvitees: Asks "Publish with no invitees?", which reports the answer to
    ///     ``onKeepEditing()`` or ``onPublishAnyway(onPublished:)``.
    ///   - onPublished: Called once the game is saved, to leave the form.
    func onPublishClick(confirmNoInvitees: () -> Void, onPublished: () -> Void) async {
        guard uiState.canSubmit else { return }
        if uiState.invitees.isEmpty {
            confirmNoInvitees()
        } else {
            await publish(onPublished: onPublished)
        }
    }

    /// "Keep editing": the form stays exactly as it was.
    func onKeepEditing() {
        eventManager.logEvent(event: AnalyticsEvent.NoInvites(response: .keepEditing))
    }

    /// "Publish anyway".
    ///
    /// - Parameter onPublished: Called once the game is saved, to leave the form.
    func onPublishAnyway(onPublished: () -> Void) async {
        eventManager.logEvent(event: AnalyticsEvent.NoInvites(response: .publishAnyway))
        await publish(onPublished: onPublished)
    }

    private func publish(onPublished: () -> Void) async {
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
                slots: state.slots,
                invitees: state.invitees
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
        show(error)
        uiState = uiState.withSubmitting(isSubmitting: false)
    }

    private func show(_ error: any TeeTimeCaddieException) {
        toastPresenter.show(error.displayMessage.localized(args: error.messageArgs))
    }
}
