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
            GameDetailsContent(uiState: viewModel.uiState)
        }
        .task { await viewModel.load() }
    }
}

// MARK: - Content

fileprivate struct GameDetailsContent: View {
    @EnvironmentObject private var theme: AppTheme

    private let uiState: any GameDetailsUiState

    init(uiState: any GameDetailsUiState) {
        self.uiState = uiState
    }

    var body: some View {
        Group {
            switch onEnum(of: uiState) {
            case .loading:
                ContentLoadingIndicator()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .error(let error):
                TtcEmptyState(title: error.title.localized(), description: error.message.localized())
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case .content(let content):
                GameDetailsBody(uiState: content.game)
            }
        }
        .background(theme.colorScheme.surface)
    }
}

/// The game itself. Editing, adding tee times, inviting, managing, slot menus and the activity log
/// are all later work, and are left out rather than shown inert.
fileprivate struct GameDetailsBody: View {
    @EnvironmentObject private var theme: AppTheme

    private let uiState: GameUiState

    init(uiState: GameUiState) {
        self.uiState = uiState
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: GameDetailsDefaults.sectionSpacing) {
                if uiState.isOrganizer {
                    TtcChip(
                        GMR.strings().game_details_organizer_chip.localized(),
                        color: .secondary,
                        icon: .symbol(.starFill)
                    )
                }

                VStack(alignment: .leading, spacing: GameDetailsDefaults.titleSpacing) {
                    Text(uiState.course)
                        .font(.largeTitle.weight(.bold))
                        .foregroundStyle(theme.colorScheme.onSurface)
                    Text(uiState.date)
                        .font(.body)
                        .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                }

                ForEach(Array(uiState.teeTimes.enumerated()), id: \.offset) { _, teeTime in
                    TeeTimeCard(uiState: teeTime)
                }
            }
            .padding(GameDetailsDefaults.padding)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

fileprivate struct TeeTimeCard: View {
    @EnvironmentObject private var theme: AppTheme

    private let uiState: GameTeeTimeUiState

    init(uiState: GameTeeTimeUiState) {
        self.uiState = uiState
    }

    private var lastRow: Int { uiState.players.count + Int(uiState.openSlots) - 1 }

    var body: some View {
        TtcCard(contentPadding: TtcCardStyle.noContentPadding) {
            VStack(spacing: 0) {
                // The tonal header strip: when, and how full.
                HStack(spacing: GameDetailsDefaults.headerSpacing) {
                    Image(SfSymbol.clock)
                        .foregroundStyle(theme.colorScheme.primary)
                    Text(uiState.time)
                        .font(.headline)
                        .foregroundStyle(theme.colorScheme.onSurface)
                    Spacer()
                    TtcChip(uiState.filledCount.localized(), size: .small)
                }
                .padding(.horizontal, GameDetailsDefaults.horizontalPadding)
                .padding(.vertical, GameDetailsDefaults.verticalPadding)
                .background(theme.colorScheme.surfaceContainerHigh)

                // The roster, then the empty slots that are left. Every row but the last carries a
                // divider.
                ForEach(Array(uiState.players.enumerated()), id: \.offset) { index, player in
                    PlayerRow(uiState: player, showDivider: index < lastRow)
                }
                ForEach(0..<Int(uiState.openSlots), id: \.self) { index in
                    EmptySlotRow(showDivider: uiState.players.count + index < lastRow)
                }
            }
        }
    }
}

fileprivate struct PlayerRow: View {
    @EnvironmentObject private var theme: AppTheme

    private let uiState: TeeTimePlayerRowUiState
    private let showDivider: Bool

    init(uiState: TeeTimePlayerRowUiState, showDivider: Bool) {
        self.uiState = uiState
        self.showDivider = showDivider
    }

    var body: some View {
        SlotRow(showDivider: showDivider) {
            TtcAvatar(
                uiState.initial,
                color: uiState.tone.toColorRole(),
                size: GameDetailsDefaults.avatarSize
            )
        } text: {
            VStack(alignment: .leading, spacing: GameDetailsDefaults.nameStatusSpacing) {
                Text(uiState.name)
                    .font(.body.weight(.medium))
                    .foregroundStyle(theme.colorScheme.onSurface)
                switch uiState.status {
                case .organizer:
                    Text(GMR.strings().game_details_organizer_label.localized())
                        .font(.caption)
                        .foregroundStyle(theme.colorScheme.secondary)
                case .invited:
                    Text(GMR.strings().game_details_invited_label.localized())
                        .font(.caption)
                        .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                case .confirmed:
                    EmptyView()
                }
            }
        }
    }
}

fileprivate struct EmptySlotRow: View {
    @EnvironmentObject private var theme: AppTheme

    private let showDivider: Bool

    init(showDivider: Bool) {
        self.showDivider = showDivider
    }

    var body: some View {
        SlotRow(showDivider: showDivider) {
            TtcAvatar(placeholder: .empty, size: GameDetailsDefaults.avatarSize)
        } text: {
            Text(GMR.strings().game_details_empty_slot.localized())
                .font(.body)
                .foregroundStyle(theme.colorScheme.onSurfaceVariant)
        }
    }
}

/// The layout every slot row shares: the avatar, then the text with an optional divider beneath it.
///
/// The row's vertical padding sits inside the text column rather than on the row, so the divider runs
/// along the row's bottom edge and starts exactly where the text does, with no inset to keep in sync.
fileprivate struct SlotRow<Avatar: View, Content: View>: View {
    @EnvironmentObject private var theme: AppTheme

    private let showDivider: Bool
    private let avatar: Avatar
    private let text: Content

    init(
        showDivider: Bool,
        @ViewBuilder avatar: () -> Avatar,
        @ViewBuilder text: () -> Content
    ) {
        self.showDivider = showDivider
        self.avatar = avatar()
        self.text = text()
    }

    var body: some View {
        HStack(spacing: GameDetailsDefaults.rowSpacing) {
            avatar
            VStack(spacing: 0) {
                text
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.vertical, GameDetailsDefaults.verticalPadding)
                    .padding(.trailing, GameDetailsDefaults.horizontalPadding)
                if showDivider {
                    // The system divider, tinted to the theme's outline rather than the system separator.
                    Divider().overlay(theme.colorScheme.outlineVariant)
                }
            }
        }
        .padding(.leading, GameDetailsDefaults.horizontalPadding)
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
}

// MARK: - Previews

fileprivate let previewDetails = GameDetailsUiStateContent(game: GameDetailsUiStateKt.previewGameUiState)

#Preview("Organizer") {
    TeeTimeCaddieTheme {
        GameDetailsContent(uiState: previewDetails)
    }
}

#Preview("With invitees") {
    TeeTimeCaddieTheme {
        GameDetailsContent(
            uiState: GameDetailsUiStateContent(game: GameDetailsUiStateKt.previewGameWithInviteesUiState)
        )
    }
}

#Preview("Organizer - Dark") {
    TeeTimeCaddieTheme {
        GameDetailsContent(uiState: previewDetails)
    }
    .preferredColorScheme(.dark)
}
