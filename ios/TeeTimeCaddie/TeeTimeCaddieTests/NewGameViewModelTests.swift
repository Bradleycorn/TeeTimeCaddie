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
    private var viewModel: NewGameViewModel!

    private let date = LocalDate(year: 2026, month: 6, day: 13).toDate()
    private let time = LocalTime(hour: 9, minute: 0, second: 0, nanosecond: 0).toDate()

    override func setUp() {
        super.setUp()
        repository = FakeGamesRepository()
        toastPresenter = TtcToastPresenter()
        viewModel = NewGameViewModel(
            gamesRepository: repository,
            currentPlayer: { TestData.organizer },
            toastPresenter: toastPresenter
        )
    }

    private func fillIn(course: String = "Pebble Beach") {
        viewModel.onCourseChange(course)
        viewModel.onDateSelected(date)
        viewModel.onTimeSelected(time)
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

    func testPublishClickAsksFirstAndPublishesNothing() {
        fillIn()

        viewModel.onPublishClick()

        XCTAssertTrue(viewModel.uiState.showNoInviteeConfirm)
        XCTAssertTrue(repository.publishRequests.isEmpty)
    }

    func testPublishClickDoesNothingWhenTheFormIsIncomplete() {
        viewModel.onCourseChange("Pebble Beach")

        viewModel.onPublishClick()

        XCTAssertFalse(viewModel.uiState.showNoInviteeConfirm)
    }

    func testKeepEditingClosesTheAlertAndLeavesTheForm() {
        fillIn()
        viewModel.onSlotsChange(3)
        viewModel.onPublishClick()
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
        viewModel.onPublishClick()

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
        viewModel.onPublishClick()
        var published = false

        await viewModel.onPublishAnyway(onPublished: { published = true })

        XCTAssertTrue(published)
        XCTAssertEqual(toastPresenter.message, "Pebble Beach game published")
    }

    func testPublishFailureReportsTheErrorAndStays() async {
        repository.publishFailure = .publishFailed
        fillIn()
        viewModel.onPublishClick()
        var published = false

        await viewModel.onPublishAnyway(onPublished: { published = true })

        XCTAssertFalse(published)
        XCTAssertEqual(toastPresenter.message, GameErrors.publishFailed.message.localized())
        XCTAssertFalse(viewModel.uiState.isSubmitting)
        XCTAssertEqual(viewModel.uiState.course, "Pebble Beach")
    }
}
