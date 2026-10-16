//
//  GamesListViewModelTests.swift
//  TeeTimeCaddieTests
//

import XCTest
import TeeTimeCaddieKit
@testable import TeeTimeCaddie

/// The Swift twin of `GamesListViewModelTest.kt`.
///
/// The live stream isn't driven here: `upcomingGames` returns a Kotlin `Flow`, and SKIE gives Swift
/// no public way to build one, so a Swift fake can't serve it. What the tab shows for a given set
/// of games is the part this ViewModel owns, and that is a pure function.
@MainActor
final class GamesListViewModelTests: XCTestCase {

    func testStartsLoading() {
        let viewModel = GamesListViewModel(
            gamesRepository: FakeGamesRepository(),
            currentPlayer: { TestData.organizer },
            eventManager: TestData.eventManager
        )

        XCTAssertEqual(viewModel.uiState, .loading)
    }

    func testNoGamesIsTheEmptyState() {
        XCTAssertEqual(GamesListViewModel.uiState(for: []), .empty)
    }

    func testGamesBecomeRowsWithTheirCourseDateAndTime() {
        let game = TestData.game(course: "Pebble Beach", date: LocalDate(year: 2026, month: 6, day: 13))

        XCTAssertEqual(
            GamesListViewModel.uiState(for: [game]),
            .content([GameRowUiModel(id: game.id, course: "Pebble Beach", dateAndTime: "Sat, Jun 13 · 9:00 AM")])
        )
    }

    func testAddGameClickIsLogged() {
        let analytics = RecordingEventManager()
        let viewModel = GamesListViewModel(
            gamesRepository: FakeGamesRepository(),
            currentPlayer: { TestData.organizer },
            eventManager: analytics.eventManager
        )

        viewModel.onAddGameClick()

        XCTAssertEqual(analytics.events, [AnalyticsEvent.AddGameClick.shared])
    }
}
