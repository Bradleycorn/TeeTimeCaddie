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

    /// The people picked so far, in the order they were added. Never affects ``canSubmit``, and may
    /// outnumber ``slots``.
    var invitees: [NewGameInviteeRow] = []

    /// Whether the "Publish with no invitees?" alert is up. Only a publish with no invitees asks.
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

    /// "Add from contacts", or "Add more from contacts" once anyone has been added. Computed, so
    /// building this state still never reaches into the Kotlin framework.
    var addFromContactsLabel: StringResource {
        invitees.isEmpty
            ? GMR.strings().new_game_add_from_contacts
            : GMR.strings().new_game_add_more_from_contacts
    }
}

/// One invitee on the New game form, ready to display. Swift-only, like the rest of
/// ``NewGameUiState``; the ViewModel builds it from the SDK's `NewInvitee`, which does the
/// formatting.
struct NewGameInviteeRow: Equatable, Identifiable {
    /// The invitee's name and number together, which is what tells two invitees apart.
    let id: String
    let name: String
    let initial: String
    let phone: String
    let tone: TtcColorRole
}

/// The Swift twin of `NewGameViewModel.kt` — same state, same method names.
@MainActor
@Observable
final class NewGameViewModel {

    private(set) var uiState = NewGameUiState()

    /// The invitees as the SDK knows them, for publishing. ``NewGameUiState/invitees`` mirrors this
    /// list row for row.
    private var invitees: [NewInvitee] = [] {
        didSet { uiState.invitees = invitees.map(\.row) }
    }

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
        uiState.course = course
    }

    func onDateSelected(_ date: Date) {
        uiState.date = date
        uiState.dateText = date.toLocalDate().weekdayMonthDay
    }

    func onTimeSelected(_ time: Date) {
        uiState.time = time
        uiState.timeText = time.toLocalTime().formattedTime
    }

    func onSlotsChange(_ slots: Int) {
        eventManager.logEvent(event: AnalyticsEvent.PlayerSlotsClick(playerCount: Int32(slots)))
        uiState.slots = slots
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
            let result = try await repository.addPickedContacts(invitees: invitees)
            switch onEnum(of: result) {
            case .success(let success):
                invitees = success.data as? [NewInvitee] ?? invitees
            case .failure(let failure):
                show(failure.error)
            }
        } catch {
            show(error.asTeeTimeCaddieException())
        }
    }

    /// An invitee's ✕ was tapped. They're removed at once, without asking.
    func onRemoveInvitee(_ id: NewGameInviteeRow.ID) {
        eventManager.logEvent(event: AnalyticsEvent.RemoveInviteeClick.shared)
        invitees.removeAll { $0.row.id == id }
    }

    /// Publish was tapped. With nobody invited, ask first; otherwise publish.
    ///
    /// - Parameter onPublished: Called once the game is saved, to leave the form.
    func onPublishClick(onPublished: () -> Void) async {
        guard uiState.canSubmit else { return }
        if invitees.isEmpty {
            uiState.showNoInviteeConfirm = true
        } else {
            await publish(onPublished: onPublished)
        }
    }

    /// "Keep editing": close the alert and leave the form exactly as it was.
    func onKeepEditing() {
        eventManager.logEvent(event: AnalyticsEvent.NoInvites(response: .keepEditing))
        onDismissNoInviteeConfirm()
    }

    /// The alert closed. Every way it closes ends here, buttons included, so it logs nothing itself.
    func onDismissNoInviteeConfirm() {
        uiState.showNoInviteeConfirm = false
    }

    /// "Publish anyway".
    ///
    /// - Parameter onPublished: Called once the game is saved, to leave the form.
    func onPublishAnyway(onPublished: () -> Void) async {
        eventManager.logEvent(event: AnalyticsEvent.NoInvites(response: .publishAnyway))
        uiState.showNoInviteeConfirm = false
        await publish(onPublished: onPublished)
    }

    private func publish(onPublished: () -> Void) async {
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
                slots: Int32(state.slots),
                invitees: invitees
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
        uiState.isSubmitting = false
    }

    private func show(_ error: any TeeTimeCaddieException) {
        toastPresenter.show(error.displayMessage.localized(args: error.messageArgs))
    }
}

private extension NewInvitee {
    var row: NewGameInviteeRow {
        NewGameInviteeRow(
            id: "\(name)|\(phone)",
            name: name,
            initial: initial,
            phone: formattedPhone,
            tone: avatarTone.toColorRole()
        )
    }
}
