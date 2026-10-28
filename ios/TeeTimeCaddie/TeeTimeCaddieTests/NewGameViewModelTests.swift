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
    private let jeff = NewInvitee(name: "Jeff Morrison", phone: "+1 415 555 0142")

    func testPickedInviteesShowAsRowsAndReplaceTheList() {
        viewModel.onInviteesPicked([matt, jeff])
        viewModel.onInviteesPicked([jeff])

        XCTAssertEqual(viewModel.uiState.invitees, [
            NewGameInviteeRow(
                id: "Jeff Morrison|+1 415 555 0142",
                name: "Jeff Morrison",
                initial: "J",
                phone: "(415) 555-0142",
                tone: jeff.avatarTone.toColorRole()
            )
        ])
    }

    func testRemovingAnInviteeKeepsTheRestInOrder() {
        let dana = NewInvitee(name: "Dana Park", phone: "4155550117")
        viewModel.onInviteesPicked([matt, jeff, dana])

        viewModel.onRemoveInvitee("Jeff Morrison|+1 415 555 0142")

        XCTAssertEqual(viewModel.uiState.invitees.map(\.name), ["Matt Chen", "Dana Park"])
    }

    func testAddFromContactsLabelReadsAddMoreOnceAnyoneIsAdded() {
        XCTAssertEqual(viewModel.uiState.addFromContactsLabel, GMR.strings().new_game_add_from_contacts)

        viewModel.onInviteesPicked([matt])
        XCTAssertEqual(viewModel.uiState.addFromContactsLabel, GMR.strings().new_game_add_more_from_contacts)

        viewModel.onRemoveInvitee(viewModel.uiState.invitees[0].id)
        XCTAssertEqual(viewModel.uiState.addFromContactsLabel, GMR.strings().new_game_add_from_contacts)
    }

    func testInviteesNeverAffectWhetherPublishIsEnabled() {
        viewModel.onInviteesPicked([matt])
        XCTAssertFalse(viewModel.uiState.canSubmit)

        fillIn()
        viewModel.onRemoveInvitee(viewModel.uiState.invitees[0].id)
        XCTAssertTrue(viewModel.uiState.canSubmit)
    }

    func testPublishClickWithInviteesPublishesThemWithoutAsking() async {
        fillIn()
        viewModel.onInviteesPicked([matt, jeff])
        var published = false

        await viewModel.onPublishClick(onPublished: { published = true })

        XCTAssertFalse(viewModel.uiState.showNoInviteeConfirm)
        XCTAssertTrue(published)
        XCTAssertEqual(repository.publishRequests.map(\.inviteeNames), [["Matt Chen", "Jeff Morrison"]])
    }

    func testPublishClickAsksAgainOnceEveryInviteeIsRemoved() async {
        fillIn()
        viewModel.onInviteesPicked([matt])
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
