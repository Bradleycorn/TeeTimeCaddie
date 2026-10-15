//
//  GamesListViewModel.swift
//  TeeTimeCaddie
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

/// One game on the Games tab, already formatted.
///
/// Free of SDK types so ``GamesListContent`` previews are pure construction.
struct GameRowUiModel: Equatable, Identifiable {
    let id: String
    let course: String

    /// "Sat, Jun 13 · 9:00 AM"
    let dateAndTime: String
}

/// State for the Games tab: a spinner, the empty state, or the list. Never two at once.
enum GamesListUiState: Equatable {
    case loading
    case empty
    case content([GameRowUiModel])
}

/// The Swift twin of `GamesListViewModel.kt`.
@MainActor
@Observable
final class GamesListViewModel {

    private(set) var uiState: GamesListUiState = .loading

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
            uiState = Self.uiState(for: games)
        }
    }

    /// Records a tap on a game. Navigating to it is the screen's job.
    func onGameClick() {
        eventManager.logEvent(event: AnalyticsEvent.GameListItemClick.shared)
    }

    /// What the tab shows for a set of games.
    static func uiState(for games: [Game]) -> GamesListUiState {
        guard !games.isEmpty else { return .empty }
        return .content(games.map { game in
            GameRowUiModel(
                id: game.id,
                course: game.course.name,
                dateAndTime: GMR.strings().games_row_date_time.localized(
                    game.date.formattedDate,
                    game.teeTimes.first?.time.formattedTime ?? ""
                )
            )
        })
    }
}
