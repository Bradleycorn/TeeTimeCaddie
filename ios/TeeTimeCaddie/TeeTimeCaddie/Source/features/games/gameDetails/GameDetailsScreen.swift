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
                onAnswer: { accept in Task { await viewModel.onAnswer(accept: accept) } },
                onInviteeClick: { inviteePhone in viewModel.onInviteeClick(inviteePhone: inviteePhone) },
                onRecordResponse: { accept in Task { await viewModel.onRecordResponse(accept: accept) } },
                onResponseSheetDismiss: { viewModel.onResponseSheetDismiss() }
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
    private let onInviteeClick: (String) -> Void
    private let onRecordResponse: (Bool) -> Void
    private let onResponseSheetDismiss: () -> Void

    init(
        uiState: any GameDetailsUiState,
        onAnswer: @escaping (Bool) -> Void,
        onInviteeClick: @escaping (String) -> Void = { _ in },
        onRecordResponse: @escaping (Bool) -> Void = { _ in },
        onResponseSheetDismiss: @escaping () -> Void = {}
    ) {
        self.uiState = uiState
        self.onAnswer = onAnswer
        self.onInviteeClick = onInviteeClick
        self.onRecordResponse = onRecordResponse
        self.onResponseSheetDismiss = onResponseSheetDismiss
    }

    /// The organizer's response sheet, while the state has one open.
    private var responseSheetUiState: ResponseSheetUiState? {
        ((uiState as? GameDetailsUiStateContent)?.game as? GameUiStateOrganizer)?.responseSheet
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
                GameDetailsBody(uiState: content.game, onAnswer: onAnswer, onInviteeClick: onInviteeClick)
            }
        }
        .background(theme.colorScheme.surface)
        .ttcBottomSheet(
            item: Binding(
                get: { responseSheetUiState },
                // Only the sheet being dragged down or tapped outside sets this; picking an answer
                // closes it from the state.
                set: { sheet in if sheet == nil { onResponseSheetDismiss() } }
            )
        ) { sheet in
            ResponseSheet(uiState: sheet, onRecordResponse: onRecordResponse)
        }
    }
}

/// The game itself, in the organizer's view or an invitee's. Editing, adding tee times, inviting,
/// managing, menus on confirmed and declined rows, and the activity log are all later work, and
/// are left out rather than shown inert.
fileprivate struct GameDetailsBody: View {
    private let uiState: GameUiState
    private let onAnswer: (Bool) -> Void
    private let onInviteeClick: (String) -> Void

    init(uiState: GameUiState, onAnswer: @escaping (Bool) -> Void, onInviteeClick: @escaping (String) -> Void) {
        self.uiState = uiState
        self.onAnswer = onAnswer
        self.onInviteeClick = onInviteeClick
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
                    TeeTimeCard(uiState: teeTime, onInviteeClick: onInviteeClick)
                }

                if case .organizer(let organizer) = onEnum(of: uiState), !organizer.declined.isEmpty {
                    DeclinedSection(heading: organizer.declinedHeading, declined: organizer.declined)
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
    private let onInviteeClick: (String) -> Void

    init(uiState: GameTeeTimeUiState, onInviteeClick: @escaping (String) -> Void) {
        self.uiState = uiState
        self.onInviteeClick = onInviteeClick
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
                    PlayerRow(uiState: player, showDivider: index < lastRow, onInviteeClick: onInviteeClick)
                }
                ForEach(0..<Int(uiState.openSlots), id: \.self) { index in
                    EmptySlotRow(showDivider: uiState.players.count + index < lastRow)
                }
            }
        }
    }
}

/// One person on the roster. In the organizer's view, an invitee who hasn't answered can be tapped,
/// or their "more" button, to record their answer.
fileprivate struct PlayerRow: View {
    @EnvironmentObject private var theme: AppTheme

    private let uiState: TeeTimePlayerRowUiState
    private let showDivider: Bool
    private let onInviteeClick: (String) -> Void

    init(uiState: TeeTimePlayerRowUiState, showDivider: Bool, onInviteeClick: @escaping (String) -> Void) {
        self.uiState = uiState
        self.showDivider = showDivider
        self.onInviteeClick = onInviteeClick
    }

    var body: some View {
        if let inviteePhone = uiState.inviteePhone {
            Button(action: { onInviteeClick(inviteePhone) }) {
                row(trailing: moreButton(inviteePhone: inviteePhone))
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
        } else {
            row(trailing: EmptyView())
        }
    }

    private func moreButton(inviteePhone: String) -> some View {
        Button(action: { onInviteeClick(inviteePhone) }) {
            // iOS's "more options" symbol, where Android shows Material's vertical ⋮.
            Image(SfSymbol.ellipsisCircle)
                .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                .frame(width: GameDetailsDefaults.moreButtonSize, height: GameDetailsDefaults.moreButtonSize)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(
            LocalizedText(resource: GMR.strings().game_details_response_more, args: [uiState.name]).localized()
        )
    }

    private func row<Trailing: View>(trailing: Trailing) -> some View {
        SlotRow(showDivider: showDivider, trailing: trailing) {
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
                case .confirmed:
                    Text(GMR.strings().game_details_confirmed_label.localized())
                        .font(.caption)
                        .foregroundStyle(theme.colorScheme.tertiary)
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
        SlotRow(showDivider: showDivider, trailing: EmptyView()) {
            TtcAvatar(placeholder: .empty, size: GameDetailsDefaults.avatarSize)
        } text: {
            Text(GMR.strings().game_details_empty_slot.localized())
                .font(.body)
                .foregroundStyle(theme.colorScheme.onSurfaceVariant)
        }
    }
}

/// The layout every slot row shares: the avatar, then the text and a `trailing` control (an
/// `EmptyView` for none), with an optional divider beneath them.
///
/// The row's vertical padding sits inside the text column rather than on the row, so the divider runs
/// along the row's bottom edge and starts exactly where the text does, with no inset to keep in sync.
fileprivate struct SlotRow<Avatar: View, Content: View, Trailing: View>: View {
    @EnvironmentObject private var theme: AppTheme

    private let showDivider: Bool
    private let avatar: Avatar
    private let text: Content
    private let trailing: Trailing

    init(
        showDivider: Bool,
        trailing: Trailing,
        @ViewBuilder avatar: () -> Avatar,
        @ViewBuilder text: () -> Content
    ) {
        self.showDivider = showDivider
        self.trailing = trailing
        self.avatar = avatar()
        self.text = text()
    }

    var body: some View {
        HStack(spacing: GameDetailsDefaults.rowSpacing) {
            avatar
            VStack(spacing: 0) {
                HStack(spacing: 0) {
                    text
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.vertical, GameDetailsDefaults.verticalPadding)
                    trailing
                }
                .padding(.trailing, Trailing.self == EmptyView.self
                    ? GameDetailsDefaults.horizontalPadding
                    : GameDetailsDefaults.trailingEndPadding)
                if showDivider {
                    // The system divider, tinted to the theme's outline rather than the system separator.
                    Divider().overlay(theme.colorScheme.outlineVariant)
                }
            }
        }
        .padding(.leading, GameDetailsDefaults.horizontalPadding)
    }
}

/// Who said no, under a "Declined · N" heading. The organizer's view only.
fileprivate struct DeclinedSection: View {
    @EnvironmentObject private var theme: AppTheme

    let heading: LocalizedText
    let declined: [DeclinedInviteeUiState]

    var body: some View {
        VStack(alignment: .leading, spacing: GameDetailsDefaults.declinedHeadingSpacing) {
            HStack(spacing: GameDetailsDefaults.declinedHeadingSpacing) {
                Image(SfSymbol.personSlash)
                    .font(.subheadline)
                Text(heading.localized())
                    .font(.subheadline.weight(.semibold))
            }
            .foregroundStyle(theme.colorScheme.onSurfaceVariant)
            .padding(.horizontal, GameDetailsDefaults.declinedHeadingInset)

            TtcCard(color: .subtle, contentPadding: GameDetailsDefaults.declinedListPadding) {
                VStack(spacing: 0) {
                    ForEach(Array(declined.enumerated()), id: \.offset) { _, invitee in
                        DeclinedRow(uiState: invitee)
                    }
                }
            }
        }
    }
}

fileprivate struct DeclinedRow: View {
    @EnvironmentObject private var theme: AppTheme

    let uiState: DeclinedInviteeUiState

    var body: some View {
        HStack(spacing: GameDetailsDefaults.rowSpacing) {
            TtcAvatar(uiState.initial, color: .neutral, size: TtcAvatarStyle.smallSize)
            Text(uiState.name)
                .font(.subheadline)
                .foregroundStyle(theme.colorScheme.onSurface)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(GameDetailsDefaults.declinedRowPadding)
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
    static let moreButtonSize: CGFloat = 44
    static let trailingEndPadding: CGFloat = 4
    static let declinedHeadingInset: CGFloat = 4
    static let declinedHeadingSpacing: CGFloat = 8
    static let declinedListPadding = EdgeInsets(top: 4, leading: 0, bottom: 4, trailing: 0)
    static let declinedRowPadding = EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16)
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
