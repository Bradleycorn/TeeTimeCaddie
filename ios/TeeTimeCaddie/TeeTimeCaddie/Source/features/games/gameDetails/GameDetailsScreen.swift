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
            GameDetailsContent(
                uiState: viewModel.uiState,
                onAnswer: { accept in Task { await viewModel.onAnswer(accept: accept) } }
            )
        }
        .task { await viewModel.observeGame() }
    }
}

// MARK: - Content

fileprivate struct GameDetailsContent: View {
    @EnvironmentObject private var theme: AppTheme

    private let uiState: any GameDetailsUiState
    private let onAnswer: (Bool) -> Void

    init(uiState: any GameDetailsUiState, onAnswer: @escaping (Bool) -> Void) {
        self.uiState = uiState
        self.onAnswer = onAnswer
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
                GameDetailsBody(uiState: content.game, onAnswer: onAnswer)
            }
        }
        .background(theme.colorScheme.surface)
    }
}

/// The game itself, in the organizer's view or an invitee's. Editing, adding tee times, inviting,
/// managing, slot menus and the activity log are all later work, and are left out rather than
/// shown inert.
fileprivate struct GameDetailsBody: View {
    private let uiState: GameUiState
    private let onAnswer: (Bool) -> Void

    init(uiState: GameUiState, onAnswer: @escaping (Bool) -> Void) {
        self.uiState = uiState
        self.onAnswer = onAnswer
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: GameDetailsDefaults.sectionSpacing) {
                switch onEnum(of: uiState) {
                case .organizer(let organizer):
                    OrganizerHeader(uiState: organizer)
                case .invitee(let invitee):
                    InviteeHeader(uiState: invitee, onAnswer: onAnswer)
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

/// The "You are the organizer" chip, the course and the date.
fileprivate struct OrganizerHeader: View {
    let uiState: GameUiStateOrganizer

    var body: some View {
        TtcChip(
            GMR.strings().game_details_organizer_chip.localized(),
            color: .secondary,
            icon: .symbol(.starFill)
        )
        GameTitle(course: uiState.course, subtitle: uiState.date)
    }
}

/// The course, who organized the game, and the RSVP card when there is an invitation to show.
fileprivate struct InviteeHeader: View {
    let uiState: GameUiStateInvitee
    let onAnswer: (Bool) -> Void

    var body: some View {
        GameTitle(course: uiState.course, subtitle: uiState.organizedBy?.localized() ?? uiState.date)
        if let rsvp = uiState.rsvp {
            RsvpCard(uiState: rsvp, onAnswer: onAnswer)
        }
    }
}

fileprivate struct GameTitle: View {
    @EnvironmentObject private var theme: AppTheme

    let course: String
    let subtitle: String

    var body: some View {
        VStack(alignment: .leading, spacing: GameDetailsDefaults.titleSpacing) {
            Text(course)
                .font(.largeTitle.weight(.bold))
                .foregroundStyle(theme.colorScheme.onSurface)
            Text(subtitle)
                .font(.body)
                .foregroundStyle(theme.colorScheme.onSurfaceVariant)
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
                Text(uiState.viewerName?.localized() ?? uiState.name)
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
                case .accepted:
                    Text(GMR.strings().game_details_accepted_label.localized())
                        .font(.caption)
                        .foregroundStyle(theme.colorScheme.primary)
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

fileprivate let previewDetails = GameDetailsUiStateContent(game: GameUiStateKt.previewGameUiState)

#Preview("Organizer") {
    TeeTimeCaddieTheme {
        GameDetailsContent(uiState: previewDetails, onAnswer: { _ in })
    }
}

#Preview("With invitees") {
    TeeTimeCaddieTheme {
        GameDetailsContent(
            uiState: GameDetailsUiStateContent(game: GameUiStateKt.previewGameWithInviteesUiState),
            onAnswer: { _ in }
        )
    }
}

#Preview("Invitee") {
    TeeTimeCaddieTheme {
        GameDetailsContent(
            uiState: GameDetailsUiStateContent(game: GameUiStateKt.previewInvitedGameUiState),
            onAnswer: { _ in }
        )
    }
}

#Preview("Accepted - Dark") {
    TeeTimeCaddieTheme {
        GameDetailsContent(
            uiState: GameDetailsUiStateContent(game: GameUiStateKt.previewAcceptedGameUiState),
            onAnswer: { _ in }
        )
    }
    .preferredColorScheme(.dark)
}

#Preview("Organizer - Dark") {
    TeeTimeCaddieTheme {
        GameDetailsContent(uiState: previewDetails, onAnswer: { _ in })
    }
    .preferredColorScheme(.dark)
}
