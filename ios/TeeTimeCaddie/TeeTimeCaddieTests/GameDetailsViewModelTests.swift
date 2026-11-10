//
//  GameDetailsViewModelTests.swift
//  TeeTimeCaddieTests
//

import XCTest
import TeeTimeCaddieKit
@testable import TeeTimeCaddie

/// The Swift twin of `GameDetailsViewModelTest.kt`.
///
/// The live game isn't driven here: `observeGame` returns a Kotlin `Flow`, and SKIE gives Swift no
/// public way to build one, so a Swift fake can't serve it. What the screen shows for a given game
/// and viewer is `Game.toDetailsUiState(viewer:)`, which the SDK tests cover.
@MainActor
final class GameDetailsViewModelTests: XCTestCase {

    private func viewModel(
        _ repository: FakeGamesRepository,
        _ toastPresenter: TtcToastPresenter,
        gameId: String = "game-1"
    ) -> GameDetailsViewModel {
        GameDetailsViewModel(
            gameId: gameId,
            gamesRepository: repository,
            currentPlayer: { TestData.otherPlayer },
            toastPresenter: toastPresenter
        )
    }

    func testStartsLoading() {
        let viewModel = viewModel(FakeGamesRepository(), TtcToastPresenter())

        XCTAssertTrue(viewModel.uiState is GameDetailsUiStateLoading)
    }

    func testAYesIsSentAndConfirmedWithTheCourse() async {
        let repository = FakeGamesRepository(games: [TestData.game(course: "Pebble Beach")])
        let toastPresenter = TtcToastPresenter()

        await viewModel(repository, toastPresenter).onAnswer(accept: true)

        XCTAssertEqual(repository.respondRequests.map(\.gameId), ["game-1"])
        XCTAssertEqual(repository.respondRequests.map(\.accept), [true])
        XCTAssertEqual(toastPresenter.message, "Pebble Beach confirmed")
    }

    func testANoIsConfirmedWithoutTheCourse() async {
        let toastPresenter = TtcToastPresenter()

        await viewModel(FakeGamesRepository(games: [TestData.game()]), toastPresenter).onAnswer(accept: false)

        XCTAssertEqual(toastPresenter.message, "Invite declined")
    }

    func testAYesThatLosesTheLastSlotSaysTheGameJustFilledUp() async {
        let repository = FakeGamesRepository(games: [TestData.game()])
        repository.respondFailure = .gameFull
        let toastPresenter = TtcToastPresenter()

        await viewModel(repository, toastPresenter).onAnswer(accept: true)

        XCTAssertEqual(toastPresenter.message, "This game just filled up")
    }

    func testAMissingGameShowsItsOwnMessage() async {
        let toastPresenter = TtcToastPresenter()

        await viewModel(FakeGamesRepository(), toastPresenter, gameId: "nope").onAnswer(accept: true)

        XCTAssertEqual(toastPresenter.message, GameErrors.notFound.message.localized())
    }
}
