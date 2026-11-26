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
    private let contactsRepository: ContactsRepository
    private let currentPlayer: () -> Player?
    private let toastPresenter: TtcToastPresenter
    private let eventManager: EventManager

    init(
        gamesRepository: GamesRepository = GamesModule.shared.gamesRepository(),
        contactsRepository: ContactsRepository = GamesModule.shared.contactsRepository(),
        currentPlayer: @escaping () -> Player? = GamesModule.shared.currentPlayer(),
        toastPresenter: TtcToastPresenter = AppModule.shared.toastPresenter(),
        eventManager: EventManager = AppModule.shared.eventManager()
    ) {
        self.gamesRepository = gamesRepository
        self.contactsRepository = contactsRepository
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

    /// "Add players" was tapped: the Add players sheet opens with nothing selected.
    func onAddPlayersClick() {
        eventManager.logEvent(event: AnalyticsEvent.AddPlayersClick.shared)
        guard let organizer = currentPlayer() else { return }
        uiState = uiState.withAddPlayers(
            addPlayers: AddPlayersUiState.companion.open(organizerPhone: organizer.phone, invitees: uiState.invitees)
        )
    }

    /// "Choose from contacts" was tapped in the sheet: the system picker opens. No permission is
    /// needed.
    ///
    /// - Parameter pickContacts: Opens the picker over the sheet, which reports how it closed to
    ///   ``onContactPickerDismissed(_:)``.
    func onChooseFromContactsClick(pickContacts: () -> Void) {
        eventManager.logEvent(event: AnalyticsEvent.ChooseFromContactsClick.shared)
        pickContacts()
    }

    /// The system picker closed. `picked` reads the contacts it returned, or is nil when nothing was
    /// picked. Picked contacts join the sheet's selection; anyone already invited or selected, or at
    /// the organizer's number, is skipped without a word.
    func onContactPickerDismissed(_ picked: (any DeviceContactsReader)?) async {
        eventManager.logEvent(event: AnalyticsEvent.ContactPickerDismiss(selection: picked == nil ? .none : .some))
        guard let picked, let addPlayersState = uiState.addPlayers else { return }
        do {
            let result = try await contactsRepository.choosePickedContacts(list: addPlayersState.list, picked: picked)
            switch onEnum(of: result) {
            case .success(let success):
                updateAddPlayersState { $0.afterContactsPicked(from: addPlayersState.list, updated: success.data) }
            case .failure(let failure):
                show(failure.error)
            }
        } catch {
            show(error.asTeeTimeCaddieException())
        }
    }

    func onPlayerNameChange(_ name: String) {
        updateAddPlayersState { $0.withName(name: name) }
    }

    /// Digits only, capped at a full number, as the create-account phone field does.
    func onPlayerPhoneChange(_ phone: String) {
        updateAddPlayersState { $0.withPhone(phone: PhoneNumber.digits(phone)) }
    }

    /// The sheet's Add was tapped. A valid name and number join the selection and the form clears;
    /// otherwise the fields show what's wrong.
    func onAddEnteredPlayer() {
        eventManager.logEvent(event: AnalyticsEvent.AddEnteredPlayerClick.shared)
        guard let addPlayersState = uiState.addPlayers else { return }
        let result = contactsRepository.chooseEnteredPerson(
            list: addPlayersState.list,
            name: addPlayersState.name,
            phone: addPlayersState.phone
        )
        updateAddPlayersState { $0.afterEntry(result: result) }
    }

    /// A selected person's chip was tapped: they're taken out of the selection.
    func onUnchoosePlayer(_ person: NewInvitee) {
        eventManager.logEvent(event: AnalyticsEvent.UnchoosePlayerClick.shared)
        updateAddPlayersState { $0.unchoose(person: person) }
    }

    /// "Add N players" was tapped: everyone selected joins the invitees, and the sheet closes.
    func onAddChosenPlayers() {
        guard let addPlayersState = uiState.addPlayers else { return }
        let list = addPlayersState.list
        eventManager.logEvent(event: AnalyticsEvent.AddChosenPlayersClick(playerCount: Int32(list.chosen.count)))
        uiState = uiState
            .withInvitees(invitees: contactsRepository.inviteChosen(list: list).invited)
            .withAddPlayers(addPlayers: nil)
    }

    /// The sheet was dragged down or the scrim tapped: the selection is discarded, without asking.
    func onAddPlayersDismissed() {
        eventManager.logEvent(event: AnalyticsEvent.AddPlayersSheetDismiss.shared)
        uiState = uiState.withAddPlayers(addPlayers: nil)
    }

    /// Apply `change` to the Add players state, if the sheet is open.
    private func updateAddPlayersState(_ change: (AddPlayersUiState) -> AddPlayersUiState) {
        guard let addPlayersState = uiState.addPlayers else { return }
        uiState = uiState.withAddPlayers(addPlayers: change(addPlayersState))
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
