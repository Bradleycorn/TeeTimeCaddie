//
//  GamesNavigation.swift
//  TeeTimeCaddie
//

import SwiftUI
import TeeTimeCaddieKit

/// Destinations within the Games tab. The Swift twin of `GamesNavigation.kt`.
enum GamesDestinations: @MainActor TtcNavKey {
    case gamesList
    case newGame
    case gameDetails(id: String)

    @ViewBuilder
    func destinationView(_ navigator: any Navigator) -> some View {
        switch self {
        case .gamesList:
            GamesListScreen(
                onNewGameClick: { navigator.navigateToNewGame() },
                onGameClick: { gameId in navigator.navigateToGameDetails(id: gameId) }
            )
            // The screen draws its own brand header, so the bar is hidden; the title still names
            // the back button on pushed screens ("‹ Games").
            .navigationTitle(GMR.strings().games_title.localized())
            .toolbar(.hidden, for: .navigationBar)

        case .newGame:
            EmptyView()

        case .gameDetails:
            EmptyView()
        }
    }
}

extension Navigator {
    func navigateToGamesTab(clearBackStack: Bool = false) {
        self.navigate(to: AppTabs.teeTimes, clearBackStack: clearBackStack)
    }

    func navigateToNewGame() {
        self.navigate(to: GamesDestinations.newGame)
    }

    func navigateToGameDetails(id: String) {
        self.navigate(to: GamesDestinations.gameDetails(id: id))
    }
}
