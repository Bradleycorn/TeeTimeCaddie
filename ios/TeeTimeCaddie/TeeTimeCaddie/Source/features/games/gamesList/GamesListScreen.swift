//
//  GamesListScreen.swift
//  TeeTimeCaddie
//

import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// The Games tab: the invitations waiting for the signed-in player's answer and their upcoming
/// games, or the empty state.
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
                },
                onAnswerInvite: { invite, accept in
                    Task { await viewModel.onAnswerInvite(invite, accept: accept) }
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
    private let onAnswerInvite: (InviteRowUiState, Bool) -> Void

    /// - Parameter onCreateGameClick: The empty state's "Create a game". The bar's + belongs to the
    ///   navigation layer (`GamesDestinations`), which handles it there.
    init(
        uiState: any GamesListUiState,
        onCreateGameClick: @escaping () -> Void,
        onGameClick: @escaping (String) -> Void,
        onAnswerInvite: @escaping (InviteRowUiState, Bool) -> Void
    ) {
        self.uiState = uiState
        self.onCreateGameClick = onCreateGameClick
        self.onGameClick = onGameClick
        self.onAnswerInvite = onAnswerInvite
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
                        if !content.invites.isEmpty {
                            InviteCard(uiState: content, onInviteClick: onGameClick, onAnswer: onAnswerInvite)
                        }
                        if content.showsUpcomingHeader {
                            Text(GMR.strings().games_upcoming_header.localized())
                                .font(.headline)
                                .padding(.top, GamesListDefaults.headerTopPadding)
                        }

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

/// A game on the Games tab. The top section has its course and its date and first tee time, with a
/// chip saying how full it is. Below a divider are how many players are in and their avatars.
fileprivate struct GameRow: View {
    @EnvironmentObject private var theme: AppTheme

    private let uiState: GameRowUiState
    private let onClick: () -> Void

    init(uiState: GameRowUiState, onClick: @escaping () -> Void) {
        self.uiState = uiState
        self.onClick = onClick
    }

    var body: some View {
        TtcCard(contentPadding: TtcCardStyle.noContentPadding, onClick: onClick) {
            VStack(spacing: 0) {
                HStack(alignment: .top, spacing: GamesListDefaults.rowHeaderSpacing) {
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
                    GameStatusChip(status: uiState.status)
                }
                .padding(GamesListDefaults.rowHeaderPadding)

                // The system divider, tinted to the theme's outline rather than the system separator.
                Divider().overlay(theme.colorScheme.outlineVariant)

                HStack {
                    Label {
                        Text(uiState.playerCount.localized())
                    } icon: {
                        Image(SfSymbol.person2)
                    }
                    .font(.subheadline.weight(.medium))
                    .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                    .frame(maxWidth: .infinity, alignment: .leading)

                    TtcPlayerStack {
                        ForEach(Array(uiState.players.enumerated()), id: \.offset) { _, player in
                            TtcPlayerItem {
                                TtcAvatar(player.initial, color: player.tone.toColorRole(), size: TtcAvatarStyle.smallSize)
                            }
                        }
                        switch onEnum(of: uiState.overflow) {
                        case .none:
                            EmptyView()
                        case .add:
                            TtcPlayerAddSlot()
                        case .more(let more):
                            TtcPlayerOverflow(more.label)
                        }
                    }
                }
                .padding(GamesListDefaults.rowFooterPadding)
            }
        }
    }
}

/// "Open", "Pending" or "Full", each in its own color with its own symbol.
fileprivate struct GameStatusChip: View {
    let status: GameFillStatus

    var body: some View {
        let (color, symbol): (TtcColorRole, SfSymbol) = switch status {
        case .open: (.neutral, .personBadgePlus)
        case .pending: (.secondary, .hourglass)
        case .full: (.tertiary, .checkmarkCircle)
        }
        TtcChip(status.label.localized(), color: color, icon: .symbol(symbol))
    }
}

/// Spacing and sizes for the Games tab — the twin of `GamesListDefaults` on Android.
fileprivate enum GamesListDefaults {
    static let horizontalPadding: CGFloat = 16
    static let spacing: CGFloat = 16
    static let headerTopPadding: CGFloat = 8
    /// 22pt at the bottom: the design's 16pt card padding plus 6pt before the divider.
    static let rowHeaderPadding = EdgeInsets(top: 16, leading: 16, bottom: 22, trailing: 16)
    static let rowHeaderSpacing: CGFloat = 12
    static let rowLineSpacing: CGFloat = 4
    static let rowFooterPadding = EdgeInsets(top: 12, leading: 16, bottom: 12, trailing: 16)
}

// MARK: - Previews

fileprivate let previewContent = GamesListUiStateContent(
    games: GameRowUiStateKt.previewGameRowUiStateList,
    invites: InviteRowUiStateKt.previewInviteRowUiStateList
)

fileprivate let previewInvitesOnly = GamesListUiStateContent(
    games: [],
    invites: [InviteRowUiStateKt.previewInviteRowUiState]
)

#Preview("Content") {
    TeeTimeCaddieTheme {
        GamesListContent(uiState: previewContent, onCreateGameClick: {}, onGameClick: { _ in }, onAnswerInvite: { _, _ in })
    }
}

#Preview("Invites only") {
    TeeTimeCaddieTheme {
        GamesListContent(uiState: previewInvitesOnly, onCreateGameClick: {}, onGameClick: { _ in }, onAnswerInvite: { _, _ in })
    }
}

#Preview("Empty") {
    TeeTimeCaddieTheme {
        GamesListContent(uiState: GamesListUiStateEmpty.shared, onCreateGameClick: {}, onGameClick: { _ in }, onAnswerInvite: { _, _ in })
    }
}

#Preview("Content - Dark") {
    TeeTimeCaddieTheme {
        GamesListContent(uiState: previewContent, onCreateGameClick: {}, onGameClick: { _ in }, onAnswerInvite: { _, _ in })
    }
    .preferredColorScheme(.dark)
}
