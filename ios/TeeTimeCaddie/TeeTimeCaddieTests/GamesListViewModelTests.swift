//
//  GamesListViewModelTests.swift
//  TeeTimeCaddieTests
//

import XCTest
import TeeTimeCaddieKit
@testable import TeeTimeCaddie

/// The Swift twin of `GamesListViewModelTest.kt`.
///
/// The live stream isn't driven here: `observeGames` returns a Kotlin `Flow`, and SKIE gives Swift
/// no public way to build one, so a Swift fake can't serve it. What the tab shows for a given set of
/// games, invitations included, is `toGamesListUiState`, which the SDK tests cover.
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

    // MARK: Answering invitations

    private func invite(id: String = "game-1", course: String = "Pebble Beach") -> InviteRowUiState {
        InviteRowUiState(id: id, course: course, dateAndTime: InviteRowUiStateKt.previewInviteRowUiState.dateAndTime, players: [], isFull: false)
    }

    private func answeringViewModel(_ repository: FakeGamesRepository, _ toastPresenter: TtcToastPresenter) -> GamesListViewModel {
        GamesListViewModel(
            gamesRepository: repository,
            currentPlayer: { TestData.otherPlayer },
            eventManager: TestData.eventManager,
            toastPresenter: toastPresenter
        )
    }

    func testAYesIsSentAndConfirmedWithTheCourse() async {
        let repository = FakeGamesRepository(games: [TestData.game(course: "Pebble Beach")])
        let toastPresenter = TtcToastPresenter()

        await answeringViewModel(repository, toastPresenter).onAnswerInvite(invite(), accept: true)

        XCTAssertEqual(repository.respondRequests.map(\.gameId), ["game-1"])
        XCTAssertEqual(repository.respondRequests.map(\.accept), [true])
        XCTAssertEqual(toastPresenter.message, "Pebble Beach confirmed")
    }

    func testANoIsConfirmedWithoutTheCourse() async {
        let repository = FakeGamesRepository(games: [TestData.game()])
        let toastPresenter = TtcToastPresenter()

        await answeringViewModel(repository, toastPresenter).onAnswerInvite(invite(), accept: false)

        XCTAssertEqual(toastPresenter.message, "Invite declined")
    }

    func testAYesThatLosesTheLastSlotSaysTheGameJustFilledUp() async {
        let repository = FakeGamesRepository(games: [TestData.game()])
        repository.respondFailure = .gameFull
        let toastPresenter = TtcToastPresenter()

        await answeringViewModel(repository, toastPresenter).onAnswerInvite(invite(), accept: true)

        XCTAssertEqual(toastPresenter.message, "This game just filled up")
    }

    func testAnyOtherFailureShowsItsOwnMessage() async {
        let toastPresenter = TtcToastPresenter()

        await answeringViewModel(FakeGamesRepository(), toastPresenter).onAnswerInvite(invite(), accept: true)

        XCTAssertEqual(toastPresenter.message, GameErrors.notFound.message.localized())
    }
}
