//
//  GameDetailsViewModel.swift
//  TeeTimeCaddie
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

/// The Swift twin of `GameDetailsViewModel.kt`.
@MainActor
@Observable
final class GameDetailsViewModel {

    private(set) var uiState: any GameDetailsUiState = GameDetailsUiStateLoading.shared

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
                uiState = GameDetailsUiStateContent(game: success.data.toDetailsUiModel(viewerId: currentPlayer()?.id))
            case .failure(let failure):
                uiState = GameDetailsUiStateError(error: failure.error)
            }
        } catch {
            uiState = GameDetailsUiStateError(error: error.asTeeTimeCaddieException())
        }
    }
}
