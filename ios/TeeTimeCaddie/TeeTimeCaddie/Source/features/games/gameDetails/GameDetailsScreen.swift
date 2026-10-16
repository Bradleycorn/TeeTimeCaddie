//
//  GameDetailsScreen.swift
//  TeeTimeCaddie
//

import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// A single game's details.
struct GameDetailsScreen: View {
    @State private var viewModel: GameDetailsViewModel

    init(gameId: String) {
        self._viewModel = State(initialValue: GameDetailsViewModel(gameId: gameId))
    }

    var body: some View {
        Screen(.GameDetails(viewName: self.viewName)) {
            GameDetailsContent(state: viewModel.uiState)
        }
        .task { await viewModel.load() }
    }
}

// MARK: - Content

fileprivate struct GameDetailsContent: View {
    @EnvironmentObject private var theme: AppTheme

    private let state: GameDetailsUiState

    init(state: GameDetailsUiState) {
        self.state = state
    }

    var body: some View {
        Group {
            switch state {
            case .loading:
                ContentLoadingIndicator()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .error(let title, let message):
                TtcEmptyState(title: title, description: message)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .content(let game):
                GameDetailsBody(game: game)
            }
        }
        .background(theme.colorScheme.surface)
    }
}

/// The game itself. Editing, adding tee times, inviting, managing, slot menus and the activity log
/// are all later work, and are left out rather than shown inert.
fileprivate struct GameDetailsBody: View {
    @EnvironmentObject private var theme: AppTheme

    private let game: GameDetailsUiModel

    init(game: GameDetailsUiModel) {
        self.game = game
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: GameDetailsDefaults.sectionSpacing) {
                if game.isOrganizer {
                    TtcChip(
                        GMR.strings().game_details_organizer_chip.localized(),
                        color: .secondary,
                        icon: .symbol(.starFill)
                    )
                }

                VStack(alignment: .leading, spacing: GameDetailsDefaults.titleSpacing) {
                    Text(game.course)
                        .font(.largeTitle.weight(.bold))
                        .foregroundStyle(theme.colorScheme.onSurface)
                    Text(game.date)
                        .font(.body)
                        .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                }

                ForEach(Array(game.teeTimes.enumerated()), id: \.offset) { _, teeTime in
                    TeeTimeCard(teeTime: teeTime)
                }
            }
            .padding(GameDetailsDefaults.padding)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

fileprivate struct TeeTimeCard: View {
    @EnvironmentObject private var theme: AppTheme

    private let teeTime: GameTeeTimeUiModel

    init(teeTime: GameTeeTimeUiModel) {
        self.teeTime = teeTime
    }

    var body: some View {
        TtcCard(contentPadding: TtcCardStyle.noContentPadding) {
            VStack(spacing: 0) {
                // The tonal header strip: when, and how full.
                HStack(spacing: GameDetailsDefaults.headerSpacing) {
                    Image(SfSymbol.clock)
                        .foregroundStyle(theme.colorScheme.primary)
                    Text(teeTime.time)
                        .font(.headline)
                        .foregroundStyle(theme.colorScheme.onSurface)
                    Spacer()
                    TtcChip(teeTime.filledCount, size: .small)
                }
                .padding(.horizontal, GameDetailsDefaults.horizontalPadding)
                .padding(.vertical, GameDetailsDefaults.verticalPadding)
                .background(theme.colorScheme.surfaceContainerHigh)

                ForEach(Array(teeTime.players.enumerated()), id: \.offset) { index, player in
                    if index > 0 { RowDivider() }
                    PlayerSlotRow(player: player)
                }
                ForEach(0..<teeTime.openSlots, id: \.self) { index in
                    if index > 0 || !teeTime.players.isEmpty { RowDivider() }
                    EmptySlotRow()
                }
            }
        }
    }
}

fileprivate struct RowDivider: View {
    @EnvironmentObject private var theme: AppTheme

    var body: some View {
        Rectangle()
            .fill(theme.colorScheme.outlineVariant)
            .frame(height: GameDetailsDefaults.dividerThickness)
            // Lines the divider up with the row text, past the avatar.
            .padding(.leading, GameDetailsDefaults.dividerInset)
    }
}

fileprivate struct PlayerSlotRow: View {
    @EnvironmentObject private var theme: AppTheme

    private let player: GameSlotUiModel

    init(player: GameSlotUiModel) {
        self.player = player
    }

    var body: some View {
        HStack(spacing: GameDetailsDefaults.rowSpacing) {
            TtcAvatar(
                player.initial,
                color: player.isOrganizer ? .secondary : .primary,
                size: GameDetailsDefaults.avatarSize
            )
            VStack(alignment: .leading, spacing: GameDetailsDefaults.nameStatusSpacing) {
                Text(player.name)
                    .font(.body.weight(.medium))
                    .foregroundStyle(theme.colorScheme.onSurface)
                if player.isOrganizer {
                    Text(GMR.strings().game_details_organizer_label.localized())
                        .font(.caption)
                        .foregroundStyle(theme.colorScheme.secondary)
                }
            }
            Spacer()
        }
        .padding(.horizontal, GameDetailsDefaults.horizontalPadding)
        .padding(.vertical, GameDetailsDefaults.verticalPadding)
    }
}

fileprivate struct EmptySlotRow: View {
    @EnvironmentObject private var theme: AppTheme

    var body: some View {
        HStack(spacing: GameDetailsDefaults.rowSpacing) {
            TtcAvatar(placeholder: .empty, size: GameDetailsDefaults.avatarSize)
            Text(GMR.strings().game_details_empty_slot.localized())
                .font(.body)
                .foregroundStyle(theme.colorScheme.onSurfaceVariant)
            Spacer()
        }
        .padding(.horizontal, GameDetailsDefaults.horizontalPadding)
        .padding(.vertical, GameDetailsDefaults.verticalPadding)
    }
}

/// Spacing and sizes for Game details — the twin of `GameDetailsDefaults` on Android.
fileprivate enum GameDetailsDefaults {
    static let padding: CGFloat = 16
    static let sectionSpacing: CGFloat = 20
    static let titleSpacing: CGFloat = 8
    static let horizontalPadding: CGFloat = 16
    static let verticalPadding: CGFloat = 12
    static let headerSpacing: CGFloat = 10
    static let rowSpacing: CGFloat = 12
    static let nameStatusSpacing: CGFloat = 1
    static let avatarSize: CGFloat = 36
    static let dividerThickness: CGFloat = 1
    /// Lines the dividers up with the row text, past the avatar.
    static let dividerInset: CGFloat = horizontalPadding + avatarSize + rowSpacing
}

// MARK: - Previews

fileprivate let previewDetails = GameDetailsUiModel(
    course: "Pebble Beach",
    date: "Sat, Jun 13",
    isOrganizer: true,
    teeTimes: [
        GameTeeTimeUiModel(
            time: "9:00 AM",
            filledCount: "1/4 filled",
            players: [GameSlotUiModel(initial: "D", name: "Dana Park", isOrganizer: true)],
            openSlots: 3
        )
    ]
)

#Preview("Organizer") {
    TeeTimeCaddieTheme {
        GameDetailsContent(state: .content(previewDetails))
    }
}

#Preview("Organizer - Dark") {
    TeeTimeCaddieTheme {
        GameDetailsContent(state: .content(previewDetails))
    }
    .preferredColorScheme(.dark)
}
