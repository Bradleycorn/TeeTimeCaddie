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
                state: viewModel.uiState,
                onNewGameClick: { source in
                    viewModel.onAddGameClick(from: source)
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

    private let state: any GamesListUiState
    private let onNewGameClick: (AnalyticsEvent.AddGameClick.AddGameClickSource) -> Void
    private let onGameClick: (String) -> Void

    init(
        state: any GamesListUiState,
        onNewGameClick: @escaping (AnalyticsEvent.AddGameClick.AddGameClickSource) -> Void,
        onGameClick: @escaping (String) -> Void
    ) {
        self.state = state
        self.onNewGameClick = onNewGameClick
        self.onGameClick = onGameClick
    }

    var body: some View {
        VStack(spacing: 0) {
            header

            switch onEnum(of: state) {
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
                        action: { onNewGameClick(.emptyList) }
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
                            GameRow(game: game, onClick: { onGameClick(game.id) })
                        }
                    }
                    .padding(.horizontal, GamesListDefaults.horizontalPadding)
                    .padding(.bottom, GamesListDefaults.horizontalPadding)
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

            Button(action: { onNewGameClick(.addButton) }) {
                Image(SfSymbol.plus)
                    .font(.title3.weight(.semibold))
                    .foregroundStyle(theme.colorScheme.primary)
                    .frame(width: GamesListDefaults.addButtonSize, height: GamesListDefaults.addButtonSize)
                    .background(theme.colorScheme.surfaceContainerHigh, in: Circle())
            }
            .accessibilityLabel(GMR.strings().games_new_game_action.localized())
        }
        .padding(.horizontal, GamesListDefaults.horizontalPadding)
        .padding(.top, GamesListDefaults.headerTopPadding)
        .padding(.bottom, GamesListDefaults.headerBottomPadding)
    }
}

/// A game on the Games tab: its course, then its date and tee time.
///
/// The status chip, player count and avatar stack in the design arrive with later stories.
fileprivate struct GameRow: View {
    @EnvironmentObject private var theme: AppTheme

    private let game: GameRowUiModel
    private let onClick: () -> Void

    init(game: GameRowUiModel, onClick: @escaping () -> Void) {
        self.game = game
        self.onClick = onClick
    }

    var body: some View {
        TtcCard(onClick: onClick) {
            VStack(alignment: .leading, spacing: GamesListDefaults.rowLineSpacing) {
                Text(game.course)
                    .font(.title2.weight(.semibold))
                    .foregroundStyle(theme.colorScheme.onSurface)
                    .lineLimit(2)
                Label {
                    Text(game.dateAndTime.localized())
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
    static let headerBottomPadding: CGFloat = 12
    static let addButtonSize: CGFloat = 36
    static let rowLineSpacing: CGFloat = 4
}

// MARK: - Previews

fileprivate let previewContent = GamesListUiStateContent(games: GamesListUiStateKt.previewGameRowUiModelList)

#Preview("Content") {
    TeeTimeCaddieTheme {
        GamesListContent(state: previewContent, onNewGameClick: {_ in}, onGameClick: { _ in })
    }
}

#Preview("Empty") {
    TeeTimeCaddieTheme {
        GamesListContent(state: GamesListUiStateEmpty.shared, onNewGameClick: {_ in}, onGameClick: { _ in })
    }
}

#Preview("Content - Dark") {
    TeeTimeCaddieTheme {
        GamesListContent(state: previewContent, onNewGameClick: {_ in}, onGameClick: { _ in })
    }
    .preferredColorScheme(.dark)
}
