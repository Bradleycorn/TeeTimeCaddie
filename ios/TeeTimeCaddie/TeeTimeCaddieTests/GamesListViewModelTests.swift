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
/// no public way to build one, so a Swift fake can't serve it. What the tab shows for a given set of
/// games is `toGamesListUiState`, which the SDK tests cover.
@MainActor
final class GamesListViewModelTests: XCTestCase {

    func testStartsLoading() {
        let viewModel = GamesListViewModel(
            gamesRepository: FakeGamesRepository(),
            currentPlayer: { TestData.organizer },
            eventManager: TestData.eventManager
        )

        XCTAssertTrue(viewModel.uiState is GamesListUiStateLoading)
    }

    func testAddGameClickIsLoggedWithItsSource() {
        let analytics = RecordingEventManager()
        let viewModel = GamesListViewModel(
            gamesRepository: FakeGamesRepository(),
            currentPlayer: { TestData.organizer },
            eventManager: analytics.eventManager
        )

        viewModel.onAddGameClick(from: .addButton)
        viewModel.onAddGameClick(from: .emptyList)

        XCTAssertEqual(analytics.events, [
            AnalyticsEvent.AddGameClick(source: .addButton),
            AnalyticsEvent.AddGameClick(source: .emptyList),
        ])
    }
}
