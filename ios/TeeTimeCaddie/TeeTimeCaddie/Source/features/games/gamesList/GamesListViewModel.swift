//
//  GamesListViewModel.swift
//  TeeTimeCaddie
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

/// The Swift twin of `GamesListViewModel.kt`.
@MainActor
@Observable
final class GamesListViewModel {

    private(set) var uiState: any GamesListUiState = GamesListUiStateLoading.shared

    private let gamesRepository: GamesRepository
    private let currentPlayer: () -> Player?
    private let eventManager: EventManager

    init(
        gamesRepository: GamesRepository = GamesModule.shared.gamesRepository(),
        currentPlayer: @escaping () -> Player? = GamesModule.shared.currentPlayer(),
        eventManager: EventManager = AppModule.shared.eventManager()
    ) {
        self.gamesRepository = gamesRepository
        self.currentPlayer = currentPlayer
        self.eventManager = eventManager
    }

    /// Follows the signed-in player's upcoming games for as long as the screen is up.
    func observeGames() async {
        guard let playerId = currentPlayer()?.id else { return }
        for await games in gamesRepository.upcomingGames(playerId: playerId) {
            uiState = GamesListUiStateKt.toGamesListUiState(games)
        }
    }

    /// Records a tap on an add control (the + or "Create a game"). Navigating is the screen's job.
    func onAddGameClick(from source: AnalyticsEvent.AddGameClick.AddGameClickSource) {
        eventManager.logEvent(event: AnalyticsEvent.AddGameClick(source: source))
    }

    /// Records a tap on a game. Navigating to it is the screen's job.
    func onGameClick() {
        eventManager.logEvent(event: AnalyticsEvent.GameListItemClick.shared)
    }

}
