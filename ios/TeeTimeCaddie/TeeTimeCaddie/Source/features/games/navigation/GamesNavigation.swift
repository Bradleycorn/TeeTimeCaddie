//
//  GamesNavigation.swift
//  TeeTimeCaddie
//

import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// Destinations within the Games tab. The Swift twin of `GamesNavigation.kt`.
enum GamesDestinations: @MainActor TtcNavKey {
    case gamesList
    case newGame
    case gameDetails(id: String)

    @ViewBuilder
    func destinationView(_ navigator: any Navigator, eventManager: EventManager) -> some View {
        switch self {
        case .gamesList:
            GamesListScreen(
                onNewGameClick: { navigator.navigateToNewGame() },
                onGameClick: { gameId in navigator.navigateToGameDetails(id: gameId) }
            )
            // The title names the back button on pushed screens ("‹ Games"); the bar itself shows
            // the brand mark in its place, as Android's centered top bar does.
            .navigationTitle(GMR.strings().games_title.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .principal) {
                    BrandTitle()
                }
                ToolbarItem(placement: .topBarTrailing) {
                    AddGameButton {
                        eventManager.logEvent(event: AnalyticsEvent.AddGameClick(source: .addButton))
                        navigator.navigateToNewGame()
                    }
                }
            }

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
                    // Leaves without saving and without asking. Logged here because the toolbar
                    // lives in the navigation layer, as the iOS CLAUDE.md asks.
                    Button(GR.strings().cancel.localized()) {
                        eventManager.logEvent(event: AnalyticsEvent.NewGameCancel.shared)
                        navigator.pop()
                    }
                }
            }

        case .gameDetails(let id):
            GameDetailsScreen(gameId: id)
                .navigationTitle(GMR.strings().game_details_title.localized())
                .navigationBarTitleDisplayMode(.inline)
        }
    }
}

/// The Games tab's title: the brand mark, in the theme's primary color.
fileprivate struct BrandTitle: View {
    @EnvironmentObject private var theme: AppTheme

    var body: some View {
        Text(AR.strings().auth_brand_name.localized())
            .font(.headline.weight(.bold))
            .foregroundStyle(theme.colorScheme.primary)
    }
}

/// The Games tab's + for creating a game.
fileprivate struct AddGameButton: View {
    @EnvironmentObject private var theme: AppTheme

    private let action: () -> Void

    init(action: @escaping () -> Void) {
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            Image(SfSymbol.plus)
                .foregroundStyle(theme.colorScheme.primary)
        }
        .accessibilityLabel(GMR.strings().games_new_game_action.localized())
    }
}

extension Navigator {
    func navigateToGamesTab(clearBackStack: Bool = false) {
        self.navigate(to: AppTabs.games, clearBackStack: clearBackStack)
    }

    func navigateToNewGame() {
        self.navigate(to: GamesDestinations.newGame)
    }

    func navigateToGameDetails(id: String) {
        self.navigate(to: GamesDestinations.gameDetails(id: id))
    }
}
