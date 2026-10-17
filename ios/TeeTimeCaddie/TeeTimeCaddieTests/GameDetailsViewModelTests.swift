//
//  GameDetailsViewModelTests.swift
//  TeeTimeCaddieTests
//

import XCTest
import TeeTimeCaddieKit
@testable import TeeTimeCaddie

/// The Swift twin of `GameDetailsViewModelTest.kt`.
@MainActor
final class GameDetailsViewModelTests: XCTestCase {

    private func loaded(
        _ gameId: String,
        games: [Game] = [TestData.game()],
        viewer: Player = TestData.organizer
    ) async -> any GameDetailsUiState {
        let viewModel = GameDetailsViewModel(
            gameId: gameId,
            gamesRepository: FakeGamesRepository(games: games),
            currentPlayer: { viewer }
        )
        await viewModel.load()
        return viewModel.uiState
    }

    private func loadedGame(
        _ gameId: String = "game-1",
        games: [Game] = [TestData.game()],
        viewer: Player = TestData.organizer
    ) async throws -> GameUiState {
        let state = await loaded(gameId, games: games, viewer: viewer)
        return try XCTUnwrap(state as? GameDetailsUiStateContent, "Expected content, got \(state)").game
    }

    func testShowsTheGameWithTheOrganizerInTheFirstSlot() async throws {
        let game = try await loadedGame()

        XCTAssertEqual(game.course, "Pebble Beach")
        XCTAssertEqual(game.date, "Sat, Jun 13")
        XCTAssertTrue(game.isOrganizer)
        let teeTime = try XCTUnwrap(game.teeTimes.first)
        XCTAssertEqual(teeTime.time, "9:00 AM")
        XCTAssertEqual(teeTime.filledCount.localized(), "1/4 filled")
        XCTAssertEqual(teeTime.players, [GameSlotUiState(initial: "D", name: "Dana Park", isOrganizer: true)])
        XCTAssertEqual(teeTime.openSlots, 3)
    }

    func testAOneSlotGameHasNoOpenSlots() async throws {
        let game = try await loadedGame(games: [TestData.game(slots: 1)])

        XCTAssertEqual(game.teeTimes.first?.filledCount.localized(), "1/1 filled")
        XCTAssertEqual(game.teeTimes.first?.openSlots, 0)
    }

    func testSomeoneElseIsNotTheOrganizer() async throws {
        let game = try await loadedGame(viewer: TestData.otherPlayer)

        XCTAssertFalse(game.isOrganizer)
    }

    func testAMissingGameIsAnError() async throws {
        let state = await loaded("nope")

        let error = try XCTUnwrap(state as? GameDetailsUiStateError, "Expected an error, got \(state)")
        XCTAssertEqual(error.title.localized(), GameErrors.notFound.title.localized())
        XCTAssertEqual(error.message.localized(), GameErrors.notFound.message.localized())
    }
}
