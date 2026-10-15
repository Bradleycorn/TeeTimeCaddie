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

    let state: GameDetailsUiState

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

    let game: GameDetailsUiModel

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                if game.isOrganizer {
                    TtcChip(
                        GMR.strings().game_details_organizer_chip.localized(),
                        color: .secondary,
                        icon: .symbol(.starFill)
                    )
                }

                VStack(alignment: .leading, spacing: 8) {
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
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

fileprivate struct TeeTimeCard: View {
    @EnvironmentObject private var theme: AppTheme

    let teeTime: GameTeeTimeUiModel

    var body: some View {
        TtcCard(contentPadding: TtcCardStyle.noContentPadding) {
            VStack(spacing: 0) {
                // The tonal header strip: when, and how full.
                HStack(spacing: 10) {
                    Image(systemName: SfSymbol.clock.rawValue)
                        .foregroundStyle(theme.colorScheme.primary)
                    Text(teeTime.time)
                        .font(.headline)
                        .foregroundStyle(theme.colorScheme.onSurface)
                    Spacer()
                    TtcChip(teeTime.filledCount, size: .small)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
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
            .frame(height: 1)
            // Lines the divider up with the row text, past the avatar.
            .padding(.leading, 64)
    }
}

fileprivate struct PlayerSlotRow: View {
    @EnvironmentObject private var theme: AppTheme

    let player: GameSlotUiModel

    var body: some View {
        HStack(spacing: 12) {
            TtcAvatar(player.initial, color: player.isOrganizer ? .secondary : .primary, size: 36)
            VStack(alignment: .leading, spacing: 1) {
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
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }
}

fileprivate struct EmptySlotRow: View {
    @EnvironmentObject private var theme: AppTheme

    var body: some View {
        HStack(spacing: 12) {
            TtcAvatar(placeholder: .empty, size: 36)
            Text(GMR.strings().game_details_empty_slot.localized())
                .font(.body)
                .foregroundStyle(theme.colorScheme.onSurfaceVariant)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }
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
