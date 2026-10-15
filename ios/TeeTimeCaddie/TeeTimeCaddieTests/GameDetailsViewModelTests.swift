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
    ) async -> GameDetailsUiState {
        let viewModel = GameDetailsViewModel(
            gameId: gameId,
            gamesRepository: FakeGamesRepository(games: games),
            currentPlayer: { viewer }
        )
        await viewModel.load()
        return viewModel.uiState
    }

    func testShowsTheGameWithTheOrganizerInTheFirstSlot() async {
        let state = await loaded("game-1")

        XCTAssertEqual(state, .content(GameDetailsUiModel(
            course: "Pebble Beach",
            date: "Sat, Jun 13",
            isOrganizer: true,
            teeTimes: [GameTeeTimeUiModel(
                time: "9:00 AM",
                filledCount: "1/4 filled",
                players: [GameSlotUiModel(initial: "D", name: "Dana Park", isOrganizer: true)],
                openSlots: 3
            )]
        )))
    }

    func testAOneSlotGameHasNoOpenSlots() async {
        let state = await loaded("game-1", games: [TestData.game(slots: 1)])

        guard case .content(let game) = state else { return XCTFail("Expected content, got \(state)") }
        XCTAssertEqual(game.teeTimes.first?.filledCount, "1/1 filled")
        XCTAssertEqual(game.teeTimes.first?.openSlots, 0)
    }

    func testSomeoneElseIsNotTheOrganizer() async {
        let state = await loaded("game-1", viewer: TestData.otherPlayer)

        guard case .content(let game) = state else { return XCTFail("Expected content, got \(state)") }
        XCTAssertFalse(game.isOrganizer)
    }

    func testAMissingGameIsAnError() async {
        let state = await loaded("nope")

        XCTAssertEqual(state, .error(
            title: GameErrors.notFound.title.localized(),
            message: GameErrors.notFound.message.localized()
        ))
    }
}
