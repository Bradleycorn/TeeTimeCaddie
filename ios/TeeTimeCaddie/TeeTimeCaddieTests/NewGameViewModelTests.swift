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
            contactsRepositoryFactory: { [eventManager = analytics.eventManager] in
                ContactsRepositoryImpl(eventManager: eventManager, reader: $0)
            },
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

    /// Pick each of `invitees` from the system picker, one pick at a time, in that order.
    private func invite(_ invitees: NewInvitee...) async {
        for invitee in invitees {
            await pick(DeviceContact(
                id: invitee.name,
                name: invitee.name,
                phones: [DevicePhone(number: invitee.phone, label: "mobile", isMobile: true)]
            ))
        }
    }

    private var inviteeNames: [String] {
        viewModel.uiState.invitees.map(\.name)
    }

    func testAddFromContactsOpensTheSystemPicker() {
        var opened = false

        viewModel.onAddFromContactsClick(pickContacts: { opened = true })

        XCTAssertTrue(opened)
    }

    func testPickedContactsShowAsRows() async {
        await invite(jeff)

        XCTAssertEqual(viewModel.uiState.invitees, [
            NewGameInviteeRow(
                id: "Jeff Morrison|(415) 555-0142",
                name: "Jeff Morrison",
                initial: "J",
                phone: "(415) 555-0142",
                tone: jeff.avatarTone.toColorRole()
            )
        ])
    }

    func testPickedContactsJoinTheEndOfTheListInTheOrderPicked() async {
        await invite(matt)
        await invite(jeff, dana)

        XCTAssertEqual(inviteeNames, ["Matt Chen", "Jeff Morrison", "Dana Park"])
    }

    func testPeoplePickedTogetherJoinSortedByName() async {
        await invite(matt)

        await pick(
            DeviceContact(id: "c2", name: "Jeff Morrison", phones: [DevicePhone(number: "(415) 555-0142", label: "Mobile", isMobile: true)]),
            DeviceContact(id: "c3", name: "Dana Park", phones: [DevicePhone(number: "415.555.0117", label: "Home", isMobile: false)])
        )

        XCTAssertEqual(inviteeNames, ["Matt Chen", "Dana Park", "Jeff Morrison"])
    }

    func testPickingSomeoneAlreadyInvitedDoesNotAddThemTwice() async {
        await invite(matt)

        await invite(NewInvitee(name: "Matt Chen", phone: "+1 415 555 0188"), jeff)

        XCTAssertEqual(viewModel.uiState.invitees.map(\.phone), ["(415) 555-0188", "(415) 555-0142"])
    }

    func testAPickedContactIsInvitedOnceAtItsMobileNumber() async {
        await pick(DeviceContact(id: "c1", name: "Jeff Morrison", phones: [
            DevicePhone(number: "502-555-0199", label: "Work", isMobile: false),
            DevicePhone(number: "(415) 555-0142", label: "Mobile", isMobile: true)
        ]))

        XCTAssertEqual(viewModel.uiState.invitees.map(\.id), ["Jeff Morrison|(415) 555-0142"])
    }

    func testPickedNumbersAGameCantBePublishedWithAreLeftOut() async {
        await pick(
            DeviceContact(id: "c1", name: "Pizza Place", phones: [DevicePhone(number: "611", label: "Other", isMobile: false)]),
            DeviceContact(id: "c2", name: "  ", phones: [DevicePhone(number: "4155550101", label: "Mobile", isMobile: true)])
        )

        XCTAssertTrue(viewModel.uiState.invitees.isEmpty)
    }

    func testAFailedReadSaysSoAndLeavesTheList() async {
        await invite(matt)

        await viewModel.onContactPickerDismissed(FakeContactsReader(error: FakeContactsReader.ReadError()))

        XCTAssertEqual(inviteeNames, ["Matt Chen"])
        XCTAssertEqual(toastPresenter.message, GameErrors.contactsLoadFailed.message.localized())
    }

    func testRemovingAnInviteeKeepsTheRestInOrder() async {
        await invite(matt, jeff, dana)

        viewModel.onRemoveInvitee("Jeff Morrison|(415) 555-0142")

        XCTAssertEqual(inviteeNames, ["Matt Chen", "Dana Park"])
    }

    func testAddFromContactsLabelReadsAddMoreOnceAnyoneIsAdded() async {
        XCTAssertEqual(viewModel.uiState.addFromContactsLabel, GMR.strings().new_game_add_from_contacts)

        await invite(matt)
        XCTAssertEqual(viewModel.uiState.addFromContactsLabel, GMR.strings().new_game_add_more_from_contacts)

        viewModel.onRemoveInvitee(viewModel.uiState.invitees[0].id)
        XCTAssertEqual(viewModel.uiState.addFromContactsLabel, GMR.strings().new_game_add_from_contacts)
    }

    func testInviteesNeverAffectWhetherPublishIsEnabled() async {
        await invite(matt)
        XCTAssertFalse(viewModel.uiState.canSubmit)

        fillIn()
        viewModel.onRemoveInvitee(viewModel.uiState.invitees[0].id)
        XCTAssertTrue(viewModel.uiState.canSubmit)
    }

    func testPublishClickWithInviteesPublishesThemWithoutAsking() async {
        fillIn()
        await invite(matt, jeff)
        var published = false

        await viewModel.onPublishClick(onPublished: { published = true })

        XCTAssertFalse(viewModel.uiState.showNoInviteeConfirm)
        XCTAssertTrue(published)
        XCTAssertEqual(repository.publishRequests.map(\.inviteeNames), [["Matt Chen", "Jeff Morrison"]])
    }

    func testPublishClickAsksAgainOnceEveryInviteeIsRemoved() async {
        fillIn()
        await invite(matt)
        viewModel.onRemoveInvitee(viewModel.uiState.invitees[0].id)

        await viewModel.onPublishClick(onPublished: {})

        XCTAssertTrue(viewModel.uiState.showNoInviteeConfirm)
        XCTAssertTrue(repository.publishRequests.isEmpty)
    }

    // MARK: - The form

    func testSlotsDefaultToTheSdkMaximum() {
        XCTAssertEqual(viewModel.uiState.slots, 4)
        XCTAssertEqual(NewGameUiState.maxSlots, Int(GamesRepositoryKt.MAX_TEE_TIME_SLOTS))
    }

    func testCanSubmitNeedsACourseADateAndATime() {
        XCTAssertFalse(NewGameUiState().canSubmit)
        XCTAssertFalse(NewGameUiState(course: "Pebble Beach", date: date).canSubmit)
        XCTAssertFalse(NewGameUiState(course: "Pebble Beach", time: time).canSubmit)
        XCTAssertFalse(NewGameUiState(date: date, time: time).canSubmit)
        XCTAssertTrue(NewGameUiState(course: "Pebble Beach", date: date, time: time).canSubmit)
    }

    func testCanSubmitIgnoresACourseOfOnlySpaces() {
        XCTAssertFalse(NewGameUiState(course: "   ", date: date, time: time).canSubmit)
    }

    func testChosenValuesReadAsTheyAreDisplayed() {
        fillIn()

        XCTAssertEqual(viewModel.uiState.dateText, "Sat, Jun 13")
        XCTAssertEqual(viewModel.uiState.timeText, "9:00 AM")
    }

    // MARK: - Publishing

    func testPublishClickAsksFirstAndPublishesNothing() async {
        fillIn()

        await viewModel.onPublishClick(onPublished: {})

        XCTAssertTrue(viewModel.uiState.showNoInviteeConfirm)
        XCTAssertTrue(repository.publishRequests.isEmpty)
    }

    func testPublishClickDoesNothingWhenTheFormIsIncomplete() async {
        viewModel.onCourseChange("Pebble Beach")

        await viewModel.onPublishClick(onPublished: {})

        XCTAssertFalse(viewModel.uiState.showNoInviteeConfirm)
    }

    func testKeepEditingClosesTheAlertAndLeavesTheForm() async {
        fillIn()
        viewModel.onSlotsChange(3)
        await viewModel.onPublishClick(onPublished: {})
        let before = viewModel.uiState

        viewModel.onKeepEditing()

        var expected = before
        expected.showNoInviteeConfirm = false
        XCTAssertEqual(viewModel.uiState, expected)
        XCTAssertTrue(repository.publishRequests.isEmpty)
    }

    func testPublishAnywayPublishesWhatWasEntered() async {
        fillIn(course: " pebble beach ")
        viewModel.onSlotsChange(2)
        await viewModel.onPublishClick(onPublished: {})

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
        await viewModel.onPublishClick(onPublished: {})
        var published = false

        await viewModel.onPublishAnyway(onPublished: { published = true })

        XCTAssertTrue(published)
        XCTAssertEqual(toastPresenter.message, "Pebble Beach game published")
    }

    func testPublishFailureReportsTheErrorAndStays() async {
        repository.publishFailure = .publishFailed
        fillIn()
        await viewModel.onPublishClick(onPublished: {})
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

    func testAddFromContactsIsLogged() {
        viewModel.onAddFromContactsClick(pickContacts: {})

        XCTAssertEqual(analytics.events, [AnalyticsEvent.AddFromContactsClick.shared])
    }

    func testAPickLogsTheDismissWithASelectionAndHowManyWereAdded() async {
        await invite(matt)

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

        viewModel.onRemoveInvitee(viewModel.uiState.invitees[0].id)

        XCTAssertEqual(analytics.events.last, AnalyticsEvent.RemoveInviteeClick.shared)
    }

    func testAlertButtonsLogTheResponse() async {
        fillIn()
        await viewModel.onPublishClick(onPublished: {})
        viewModel.onKeepEditing()
        await viewModel.onPublishClick(onPublished: {})
        await viewModel.onPublishAnyway(onPublished: {})

        XCTAssertEqual(analytics.events, [
            AnalyticsEvent.NoInvites(response: .keepEditing),
            AnalyticsEvent.NoInvites(response: .publishAnyway),
        ])
    }

    func testDismissingTheAlertWithoutAButtonClosesItButLogsNothing() async {
        fillIn()
        await viewModel.onPublishClick(onPublished: {})

        viewModel.onDismissNoInviteeConfirm()

        XCTAssertFalse(viewModel.uiState.showNoInviteeConfirm)
        XCTAssertTrue(analytics.events.isEmpty)
    }
}
