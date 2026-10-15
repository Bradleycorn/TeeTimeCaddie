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
            NewGameScreen(
                // Back to the Games tab, not on to the new game's details.
                onPublished: { navigator.pop() }
            )
            .navigationTitle(GMR.strings().new_game_title.localized())
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden(true)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    // Leaves without saving and without asking.
                    Button(GR.strings().cancel.localized()) { navigator.pop() }
                }
            }

        case .gameDetails(let id):
            GameDetailsScreen(gameId: id)
                .navigationTitle(GMR.strings().game_details_title.localized())
                .navigationBarTitleDisplayMode(.inline)
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
