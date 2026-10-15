//
//  GameDetailsViewModel.swift
//  TeeTimeCaddie
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

/// One slot in a tee time: a player, or nobody yet.
struct GameSlotUiModel: Equatable {
    let initial: String
    let name: String
    let isOrganizer: Bool
}

/// One tee time, formatted.
struct GameTeeTimeUiModel: Equatable {
    let time: String

    /// "1/4 filled"
    let filledCount: String

    let players: [GameSlotUiModel]
    let openSlots: Int
}

/// A game's details, ready to draw. Free of SDK types so ``GameDetailsContent`` previews are pure
/// construction.
struct GameDetailsUiModel: Equatable {
    let course: String
    let date: String
    let isOrganizer: Bool
    let teeTimes: [GameTeeTimeUiModel]
}

enum GameDetailsUiState: Equatable {
    case loading
    case content(GameDetailsUiModel)
    /// The game could not be read, or no longer exists.
    case error(title: String, message: String)
}

/// The Swift twin of `GameDetailsViewModel.kt`.
@MainActor
@Observable
final class GameDetailsViewModel {

    private(set) var uiState: GameDetailsUiState = .loading

    private let gameId: String
    private let gamesRepository: GamesRepository
    private let currentPlayer: () -> Player?

    init(
        gameId: String,
        gamesRepository: GamesRepository = GamesModule.shared.gamesRepository(),
        currentPlayer: @escaping () -> Player? = GamesModule.shared.currentPlayer()
    ) {
        self.gameId = gameId
        self.gamesRepository = gamesRepository
        self.currentPlayer = currentPlayer
    }

    func load() async {
        do {
            let result = try await gamesRepository.getGame(gameId: gameId)
            switch onEnum(of: result) {
            case .success(let success):
                uiState = .content(Self.uiModel(for: success.data, viewerId: currentPlayer()?.id))
            case .failure(let failure):
                show(failure.error)
            }
        } catch {
            show(error.asTeeTimeCaddieException())
        }
    }

    private func show(_ error: any TeeTimeCaddieException) {
        uiState = .error(
            title: error.title.localized(),
            message: error.displayMessage.localized(args: error.messageArgs)
        )
    }

    static func uiModel(for game: Game, viewerId: String?) -> GameDetailsUiModel {
        GameDetailsUiModel(
            course: game.course.name,
            date: game.date.formattedDate,
            isOrganizer: viewerId.map { game.isOrganizer(playerId: $0) } ?? false,
            teeTimes: game.teeTimes.map { teeTime in
                GameTeeTimeUiModel(
                    time: teeTime.time.formattedTime,
                    filledCount: GMR.strings().game_details_filled_count.localized(
                        String(teeTime.filled),
                        String(teeTime.slots)
                    ),
                    players: teeTime.players.map { player in
                        GameSlotUiModel(
                            initial: player.initial,
                            name: player.name,
                            isOrganizer: player.playerId == game.organizerId
                        )
                    },
                    openSlots: Int(teeTime.openSlots)
                )
            }
        )
    }
}
