//
//  NewGameViewModelTests.swift
//  TeeTimeCaddieTests
//

import XCTest
import TeeTimeCaddieKit
@testable import TeeTimeCaddie

/// The Swift twin of `NewGameViewModelTest.kt`.
@MainActor
final class NewGameViewModelTests: XCTestCase {

    private var repository: FakeGamesRepository!
    private var toastPresenter: TtcToastPresenter!
    private var analytics: RecordingEventManager!
    private var viewModel: NewGameViewModel!

    private let date = LocalDate(year: 2026, month: 6, day: 13).toDate()
    private let time = LocalTime(hour: 9, minute: 0, second: 0, nanosecond: 0).toDate()

    override func setUp() {
        super.setUp()
        repository = FakeGamesRepository()
        toastPresenter = TtcToastPresenter()
        analytics = RecordingEventManager()
        viewModel = NewGameViewModel(
            gamesRepository: repository,
            contactsRepository: ContactsRepositoryImpl(eventManager: analytics.eventManager),
            currentPlayer: { TestData.organizer },
            toastPresenter: toastPresenter,
            eventManager: analytics.eventManager
        )
    }

    private func fillIn(course: String = "Pebble Beach") {
        viewModel.onCourseChange(course)
        viewModel.onDateSelected(date)
        viewModel.onTimeSelected(time)
    }

    // MARK: - Invitees

    private let matt = NewInvitee(name: "Matt Chen", phone: "(415) 555-0188")
    private let jeff = NewInvitee(name: "Jeff Morrison", phone: "(415) 555-0142")
    private let dana = NewInvitee(name: "Dana Park", phone: "415.555.0117")

    /// The system picker returned `contacts`.
    private func pick(_ contacts: DeviceContact...) async {
        await viewModel.onContactPickerDismissed(FakeContactsReader(contacts: contacts))
    }

    /// Choose `person` in the open sheet by picking them from the system picker.
    private func pick(_ person: NewInvitee) async {
        await pick(DeviceContact(
            id: person.name,
            name: person.name,
            phones: [DevicePhone(number: person.phone, label: "mobile", isMobile: true)]
        ))
    }

    /// Open the sheet, pick each of `invitees` one pick at a time, in that order, and add them.
    private func invite(_ invitees: NewInvitee...) async {
        viewModel.onAddPlayersClick()
        for invitee in invitees {
            await pick(invitee)
        }
        viewModel.onAddChosenPlayers()
    }

    private var addPlayersState: AddPlayersUiState? {
        viewModel.uiState.addPlayers
    }

    private var inviteeNames: [String] {
        viewModel.uiState.invitees.map(\.name)
    }

    func testAddPlayersOpensTheSheetWithNothingSelected() async {
        await invite(matt)

        viewModel.onAddPlayersClick()

        XCTAssertEqual(addPlayersState, AddPlayersUiState.companion.open(organizerPhone: TestData.organizer.phone, invitees: [matt]))
    }

    func testChooseFromContactsOpensTheSystemPicker() {
        viewModel.onAddPlayersClick()
        var opened = false

        viewModel.onChooseFromContactsClick(pickContacts: { opened = true })

        XCTAssertTrue(opened)
    }

    func testPickedContactsAreSelectedNotInvitedUntilAdded() async {
        viewModel.onAddPlayersClick()
        await pick(matt)
        await pick(jeff)

        XCTAssertEqual(addPlayersState?.chosen, [matt, jeff])
        XCTAssertTrue(viewModel.uiState.invitees.isEmpty)

        viewModel.onAddChosenPlayers()

        XCTAssertEqual(viewModel.uiState.invitees, [matt, jeff])
        XCTAssertNil(viewModel.uiState.addPlayers)
    }

    func testAddedPlayersJoinTheEndOfTheList() async {
        await invite(matt)
        await invite(jeff, dana)

        XCTAssertEqual(inviteeNames, ["Matt Chen", "Jeff Morrison", "Dana Park"])
    }

    func testPeoplePickedTogetherAreSelectedSortedByName() async {
        viewModel.onAddPlayersClick()

        await pick(
            DeviceContact(id: "c2", name: "Jeff Morrison", phones: [DevicePhone(number: "(415) 555-0142", label: "Mobile", isMobile: true)]),
            DeviceContact(id: "c3", name: "Dana Park", phones: [DevicePhone(number: "415.555.0117", label: "Home", isMobile: false)])
        )

        XCTAssertEqual(addPlayersState?.chosen, [dana, jeff])
    }

    func testPickingSomeoneAlreadyInvitedOrSelectedSkipsThem() async {
        await invite(matt)
        viewModel.onAddPlayersClick()
        await pick(jeff)

        await pick(NewInvitee(name: "Matthew Chen", phone: "+1 415 555 0188"))
        await pick(NewInvitee(name: "Jeff M", phone: "4155550142"))

        XCTAssertEqual(addPlayersState?.chosen, [jeff])
    }

    func testAPickedContactIsSelectedAtItsMobileNumber() async {
        viewModel.onAddPlayersClick()

        await pick(DeviceContact(id: "c1", name: "Jeff Morrison", phones: [
            DevicePhone(number: "502-555-0199", label: "Work", isMobile: false),
            DevicePhone(number: "(415) 555-0142", label: "Mobile", isMobile: true)
        ]))

        XCTAssertEqual(addPlayersState?.chosen, [jeff])
    }

    func testPickedNumbersAGameCantBePublishedWithAreLeftOut() async {
        viewModel.onAddPlayersClick()

        await pick(
            DeviceContact(id: "c1", name: "Pizza Place", phones: [DevicePhone(number: "611", label: "Other", isMobile: false)]),
            DeviceContact(id: "c2", name: "  ", phones: [DevicePhone(number: "4155550101", label: "Mobile", isMobile: true)])
        )

        XCTAssertEqual(addPlayersState?.chosen, [])
    }

    func testAFailedReadSaysSoAndLeavesTheSelection() async {
        viewModel.onAddPlayersClick()
        await pick(matt)

        await viewModel.onContactPickerDismissed(FakeContactsReader(error: FakeContactsReader.ReadError()))

        XCTAssertEqual(addPlayersState?.chosen, [matt])
        XCTAssertEqual(toastPresenter.message, GameErrors.contactsLoadFailed.message.localized())
    }

    func testAnEnteredPlayerIsSelectedAndTheFormClears() {
        viewModel.onAddPlayersClick()
        viewModel.onPlayerNameChange(" Sam  Tanaka ")
        viewModel.onPlayerPhoneChange("(415) 555-0123")

        viewModel.onAddEnteredPlayer()

        XCTAssertEqual(addPlayersState?.chosen, [NewInvitee(name: "Sam Tanaka", phone: "4155550123")])
        XCTAssertEqual(addPlayersState?.name, "")
        XCTAssertEqual(addPlayersState?.phone, "")
        XCTAssertNotNil(addPlayersState?.chosenMessage)
    }

    func testAnInvalidEntryShowsItsErrorsAndSelectsNobody() {
        viewModel.onAddPlayersClick()

        viewModel.onAddEnteredPlayer()

        XCTAssertEqual(addPlayersState?.chosen, [])
        XCTAssertEqual(addPlayersState?.nameError, GMR.strings().add_players_error_no_name)
        XCTAssertEqual(addPlayersState?.phoneError, GMR.strings().add_players_error_no_phone)
    }

    func testTheEnteredNumberKeepsOnlyItsFirstTenDigits() {
        viewModel.onAddPlayersClick()

        viewModel.onPlayerPhoneChange("(415) 555-01234")

        XCTAssertEqual(addPlayersState?.phone, "4155550123")
    }

    func testTappingAChipUnselectsThem() async {
        viewModel.onAddPlayersClick()
        await pick(matt)
        await pick(jeff)

        viewModel.onUnchoosePlayer(matt)

        XCTAssertEqual(addPlayersState?.chosen, [jeff])
    }

    func testDismissingTheSheetDiscardsTheSelection() async {
        await invite(matt)
        viewModel.onAddPlayersClick()
        await pick(jeff)

        viewModel.onAddPlayersDismissed()

        XCTAssertNil(viewModel.uiState.addPlayers)
        XCTAssertEqual(viewModel.uiState.invitees, [matt])
    }

    func testAPickWithTheSheetClosedChangesNothing() async {
        await pick(matt)

        XCTAssertNil(viewModel.uiState.addPlayers)
        XCTAssertTrue(viewModel.uiState.invitees.isEmpty)
    }

    func testRemovingAnInviteeKeepsTheRestInOrder() async {
        await invite(matt, jeff, dana)

        viewModel.onRemoveInvitee(jeff)

        XCTAssertEqual(inviteeNames, ["Matt Chen", "Dana Park"])
    }

    func testAddFromContactsLabelReadsAddMoreOnceAnyoneIsAdded() async {
        XCTAssertEqual(viewModel.uiState.addPlayersLabel, GMR.strings().new_game_add_players)

        await invite(matt)
        XCTAssertEqual(viewModel.uiState.addPlayersLabel, GMR.strings().new_game_add_more_players)

        viewModel.onRemoveInvitee(viewModel.uiState.invitees[0])
        XCTAssertEqual(viewModel.uiState.addPlayersLabel, GMR.strings().new_game_add_players)
    }

    func testInviteesNeverAffectWhetherPublishIsEnabled() async {
        await invite(matt)
        XCTAssertFalse(viewModel.uiState.canSubmit)

        fillIn()
        viewModel.onRemoveInvitee(viewModel.uiState.invitees[0])
        XCTAssertTrue(viewModel.uiState.canSubmit)
    }

    func testPublishClickWithInviteesPublishesThemWithoutAsking() async {
        fillIn()
        await invite(matt, jeff)
        var asked = false
        var published = false

        await viewModel.onPublishClick(confirmNoInvitees: { asked = true }, onPublished: { published = true })

        XCTAssertFalse(asked)
        XCTAssertTrue(published)
        XCTAssertEqual(repository.publishRequests.map(\.inviteeNames), [["Matt Chen", "Jeff Morrison"]])
    }

    func testPublishClickAsksAgainOnceEveryInviteeIsRemoved() async {
        fillIn()
        await invite(matt)
        viewModel.onRemoveInvitee(viewModel.uiState.invitees[0])
        var asked = false

        await viewModel.onPublishClick(confirmNoInvitees: { asked = true }, onPublished: {})

        XCTAssertTrue(asked)
        XCTAssertTrue(repository.publishRequests.isEmpty)
    }

    // MARK: - The form

    func testSlotsDefaultToTheSdkMaximum() {
        XCTAssertEqual(viewModel.uiState.slots, GamesRepositoryKt.MAX_TEE_TIME_SLOTS)
    }

    func testChosenValuesReadAsTheyAreDisplayed() {
        fillIn()

        XCTAssertEqual(viewModel.uiState.dateText, "Sat, Jun 13")
        XCTAssertEqual(viewModel.uiState.timeText, "9:00 AM")
    }

    // MARK: - Publishing

    func testPublishClickAsksFirstAndPublishesNothing() async {
        fillIn()
        var asked = false

        await viewModel.onPublishClick(confirmNoInvitees: { asked = true }, onPublished: {})

        XCTAssertTrue(asked)
        XCTAssertTrue(repository.publishRequests.isEmpty)
    }

    func testPublishClickDoesNothingWhenTheFormIsIncomplete() async {
        viewModel.onCourseChange("Pebble Beach")
        var asked = false

        await viewModel.onPublishClick(confirmNoInvitees: { asked = true }, onPublished: {})

        XCTAssertFalse(asked)
        XCTAssertTrue(repository.publishRequests.isEmpty)
    }

    func testKeepEditingLeavesTheFormAsItWas() async {
        fillIn()
        viewModel.onSlotsChange(3)
        await viewModel.onPublishClick(confirmNoInvitees: {}, onPublished: {})
        let before = viewModel.uiState

        viewModel.onKeepEditing()

        XCTAssertEqual(viewModel.uiState, before)
        XCTAssertTrue(repository.publishRequests.isEmpty)
    }

    func testPublishAnywayPublishesWhatWasEntered() async {
        fillIn(course: " pebble beach ")
        viewModel.onSlotsChange(2)
        await viewModel.onPublishClick(confirmNoInvitees: {}, onPublished: {})

        await viewModel.onPublishAnyway(onPublished: {})

        XCTAssertEqual(repository.publishRequests, [
            FakeGamesRepository.PublishRequest(
                organizerId: TestData.organizer.id,
                courseName: " pebble beach ",
                date: "2026-06-13",
                time: "09:00",
                slots: 2
            )
        ])
    }

    func testPublishAnywayConfirmsWithTheStoredSpellingAndLeaves() async {
        repository.storedCourseName = { _ in "Pebble Beach" }
        fillIn(course: "pebble beach")
        await viewModel.onPublishClick(confirmNoInvitees: {}, onPublished: {})
        var published = false

        await viewModel.onPublishAnyway(onPublished: { published = true })

        XCTAssertTrue(published)
        XCTAssertEqual(toastPresenter.message, "Pebble Beach game published")
    }

    func testPublishFailureReportsTheErrorAndStays() async {
        repository.publishFailure = .publishFailed
        fillIn()
        await viewModel.onPublishClick(confirmNoInvitees: {}, onPublished: {})
        var published = false

        await viewModel.onPublishAnyway(onPublished: { published = true })

        XCTAssertFalse(published)
        XCTAssertEqual(toastPresenter.message, GameErrors.publishFailed.message.localized())
        XCTAssertFalse(viewModel.uiState.isSubmitting)
        XCTAssertEqual(viewModel.uiState.course, "Pebble Beach")
    }

    // MARK: - Analytics

    func testSelectingSlotsLogsTheCountTapped() {
        viewModel.onSlotsChange(3)

        XCTAssertEqual(analytics.events, [AnalyticsEvent.PlayerSlotsClick(playerCount: 3)])
    }

    func testAddPlayersIsLogged() {
        viewModel.onAddPlayersClick()

        XCTAssertEqual(analytics.events, [AnalyticsEvent.AddPlayersClick.shared])
    }

    func testAPickLogsTheDismissWithASelectionAndHowManyWereAdded() async {
        await invite(matt)
        viewModel.onAddPlayersClick()

        await pick(
            DeviceContact(id: "c1", name: "Matt Chen", phones: [DevicePhone(number: "4155550188", label: "Mobile", isMobile: true)]),
            DeviceContact(id: "c2", name: "Jeff Morrison", phones: [DevicePhone(number: "(415) 555-0142", label: "Mobile", isMobile: true)])
        )

        XCTAssertEqual(Array(analytics.events.suffix(2)), [
            AnalyticsEvent.ContactPickerDismiss(selection: .some),
            AnalyticsEvent.AddInvitees(pickedCount: 2, addedCount: 1),
        ])
    }

    func testClosingThePickerWithNothingPickedLogsADismissWithNoSelection() async {
        await invite(matt)
        let before = analytics.events.count

        await viewModel.onContactPickerDismissed(nil)

        XCTAssertEqual(Array(analytics.events.dropFirst(before)), [AnalyticsEvent.ContactPickerDismiss(selection: .none)])
        XCTAssertEqual(viewModel.uiState.invitees.map(\.name), ["Matt Chen"])
    }

    func testRemovingAnInviteeIsLogged() async {
        await invite(matt)

        viewModel.onRemoveInvitee(viewModel.uiState.invitees[0])

        XCTAssertEqual(analytics.events.last, AnalyticsEvent.RemoveInviteeClick.shared)
    }

    func testAlertButtonsLogTheResponse() async {
        fillIn()
        await viewModel.onPublishClick(confirmNoInvitees: {}, onPublished: {})
        viewModel.onKeepEditing()
        await viewModel.onPublishClick(confirmNoInvitees: {}, onPublished: {})
        await viewModel.onPublishAnyway(onPublished: {})

        XCTAssertEqual(analytics.events, [
            AnalyticsEvent.NoInvites(response: .keepEditing),
            AnalyticsEvent.NoInvites(response: .publishAnyway),
        ])
    }
}
