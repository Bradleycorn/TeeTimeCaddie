//
//  GamesListScreen.swift
//  TeeTimeCaddie
//

import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// The Games tab: the signed-in player's upcoming games, or the empty state.
struct GamesListScreen: View {
    private let onNewGameClick: () -> Void
    private let onGameClick: (String) -> Void

    @State private var viewModel = GamesListViewModel()

    init(onNewGameClick: @escaping () -> Void, onGameClick: @escaping (String) -> Void) {
        self.onNewGameClick = onNewGameClick
        self.onGameClick = onGameClick
    }

    var body: some View {
        Screen(.Games(viewName: self.viewName)) {
            GamesListContent(
                uiState: viewModel.uiState,
                onCreateGameClick: {
                    viewModel.onAddGameClick(from: .emptyList)
                    onNewGameClick()
                },
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

    private let uiState: any GamesListUiState
    private let onCreateGameClick: () -> Void
    private let onGameClick: (String) -> Void

    /// - Parameter onCreateGameClick: The empty state's "Create a game". The bar's + belongs to the
    ///   navigation layer (`GamesDestinations`), which handles it there.
    init(
        uiState: any GamesListUiState,
        onCreateGameClick: @escaping () -> Void,
        onGameClick: @escaping (String) -> Void
    ) {
        self.uiState = uiState
        self.onCreateGameClick = onCreateGameClick
        self.onGameClick = onGameClick
    }

    var body: some View {
        Group {
            switch onEnum(of: uiState) {
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
                        action: onCreateGameClick
                    )
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .content(let content):
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: GamesListDefaults.spacing) {
                        Text(GMR.strings().games_upcoming_header.localized())
                            .font(.headline)
                            .padding(.top, GamesListDefaults.headerTopPadding)

                        ForEach(content.games, id: \.id) { game in
                            GameRow(uiState: game, onClick: { onGameClick(game.id) })
                        }
                    }
                    .padding(.horizontal, GamesListDefaults.horizontalPadding)
                    .padding(.bottom, GamesListDefaults.horizontalPadding)
                }
            }
        }
        .background(theme.colorScheme.surface)
    }
}

/// A game on the Games tab: its course, then its date and tee time.
///
/// The status chip, player count and avatar stack in the design arrive with later stories.
fileprivate struct GameRow: View {
    @EnvironmentObject private var theme: AppTheme

    private let uiState: GameRowUiState
    private let onClick: () -> Void

    init(uiState: GameRowUiState, onClick: @escaping () -> Void) {
        self.uiState = uiState
        self.onClick = onClick
    }

    var body: some View {
        TtcCard(onClick: onClick) {
            VStack(alignment: .leading, spacing: GamesListDefaults.rowLineSpacing) {
                Text(uiState.course)
                    .font(.title2.weight(.semibold))
                    .foregroundStyle(theme.colorScheme.onSurface)
                    .lineLimit(2)
                Label {
                    Text(uiState.dateAndTime.localized())
                } icon: {
                    Image(SfSymbol.clock)
                }
                .font(.subheadline)
                .foregroundStyle(theme.colorScheme.onSurfaceVariant)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

/// Spacing and sizes for the Games tab — the twin of `GamesListDefaults` on Android.
fileprivate enum GamesListDefaults {
    static let horizontalPadding: CGFloat = 16
    static let spacing: CGFloat = 16
    static let headerTopPadding: CGFloat = 8
    static let rowLineSpacing: CGFloat = 4
}

// MARK: - Previews

fileprivate let previewContent = GamesListUiStateContent(games: GamesListUiStateKt.previewGameRowUiStateList)

#Preview("Content") {
    TeeTimeCaddieTheme {
        GamesListContent(uiState: previewContent, onCreateGameClick: {}, onGameClick: { _ in })
    }
}

#Preview("Empty") {
    TeeTimeCaddieTheme {
        GamesListContent(uiState: GamesListUiStateEmpty.shared, onCreateGameClick: {}, onGameClick: { _ in })
    }
}

#Preview("Content - Dark") {
    TeeTimeCaddieTheme {
        GamesListContent(uiState: previewContent, onCreateGameClick: {}, onGameClick: { _ in })
    }
    .preferredColorScheme(.dark)
}
