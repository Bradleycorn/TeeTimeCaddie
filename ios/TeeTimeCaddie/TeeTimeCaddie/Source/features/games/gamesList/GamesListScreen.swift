//
//  GamesListScreen.swift
//  TeeTimeCaddie
//

import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// The Games tab: the signed-in player's upcoming games, or the empty state.
struct GamesListScreen: View {
    let onNewGameClick: () -> Void
    let onGameClick: (String) -> Void

    @State private var viewModel = GamesListViewModel()

    var body: some View {
        Screen(.Games(viewName: self.viewName)) {
            GamesListContent(
                state: viewModel.uiState,
                onNewGameClick: onNewGameClick,
                onGameClick: { gameId in
                    viewModel.onGameClick()
                    onGameClick(gameId)
                }
            )
        }
        .task { await viewModel.observeGames() }
    }
}

// MARK: - Content

fileprivate struct GamesListContent: View {
    @EnvironmentObject private var theme: AppTheme

    let state: GamesListUiState
    let onNewGameClick: () -> Void
    let onGameClick: (String) -> Void

    var body: some View {
        VStack(spacing: 0) {
            header

            switch state {
            case .loading:
                ContentLoadingIndicator()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .empty:
                TtcEmptyState(
                    title: GMR.strings().games_empty_title.localized(),
                    icon: .symbol(.flag),
                    description: GMR.strings().games_empty_message.localized()
                ) {
                    TtcButton(
                        GMR.strings().games_create_button.localized(),
                        icon: .symbol(.plus),
                        action: onNewGameClick
                    )
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .content(let games):
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: Metrics.spacing) {
                        Text(GMR.strings().games_upcoming_header.localized())
                            .font(.headline)
                            .padding(.top, Metrics.headerTopPadding)

                        ForEach(games) { game in
                            GameRow(game: game, onClick: { onGameClick(game.id) })
                        }
                    }
                    .padding(.horizontal, Metrics.horizontalPadding)
                    .padding(.bottom, Metrics.horizontalPadding)
                }
            }
        }
        .background(theme.colorScheme.surface)
    }

    /// The brand as a large title, with the round + beside it, as the design draws it.
    private var header: some View {
        HStack(alignment: .lastTextBaseline) {
            Text(AR.strings().auth_brand_name.localized())
                .font(.largeTitle.weight(.bold))
                .foregroundStyle(theme.colorScheme.primary)

            Spacer()

            Button(action: onNewGameClick) {
                Image(systemName: SfSymbol.plus.rawValue)
                    .font(.title3.weight(.semibold))
                    .foregroundStyle(theme.colorScheme.primary)
                    .frame(width: Metrics.addButtonSize, height: Metrics.addButtonSize)
                    .background(theme.colorScheme.surfaceContainerHigh, in: Circle())
            }
            .accessibilityLabel(GMR.strings().games_new_game_action.localized())
        }
        .padding(.horizontal, Metrics.horizontalPadding)
        .padding(.top, Metrics.headerTopPadding)
        .padding(.bottom, Metrics.headerBottomPadding)
    }

    private enum Metrics {
        static let horizontalPadding: CGFloat = 16
        static let spacing: CGFloat = 16
        static let headerTopPadding: CGFloat = 8
        static let headerBottomPadding: CGFloat = 12
        static let addButtonSize: CGFloat = 36
    }
}

/// A game on the Games tab: its course, then its date and tee time.
///
/// The status chip, player count and avatar stack in the design arrive with later stories.
fileprivate struct GameRow: View {
    @EnvironmentObject private var theme: AppTheme

    let game: GameRowUiModel
    let onClick: () -> Void

    var body: some View {
        TtcCard(onClick: onClick) {
            VStack(alignment: .leading, spacing: 4) {
                Text(game.course)
                    .font(.title2.weight(.semibold))
                    .foregroundStyle(theme.colorScheme.onSurface)
                    .lineLimit(2)
                Label {
                    Text(game.dateAndTime)
                } icon: {
                    Image(systemName: SfSymbol.clock.rawValue)
                }
                .font(.subheadline)
                .foregroundStyle(theme.colorScheme.onSurfaceVariant)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

// MARK: - Previews

fileprivate let previewRows = [
    GameRowUiModel(id: "1", course: "Pebble Beach", dateAndTime: "Sat, Jun 13 · 9:00 AM"),
    GameRowUiModel(id: "2", course: "Bethpage Black", dateAndTime: "Sun, Jun 14 · 8:10 AM"),
]

#Preview("Content") {
    TeeTimeCaddieTheme {
        GamesListContent(state: .content(previewRows), onNewGameClick: {}, onGameClick: { _ in })
    }
}

#Preview("Empty") {
    TeeTimeCaddieTheme {
        GamesListContent(state: .empty, onNewGameClick: {}, onGameClick: { _ in })
    }
}

#Preview("Content - Dark") {
    TeeTimeCaddieTheme {
        GamesListContent(state: .content(previewRows), onNewGameClick: {}, onGameClick: { _ in })
    }
    .preferredColorScheme(.dark)
}
