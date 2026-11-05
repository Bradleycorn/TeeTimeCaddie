//
//  InviteCard.swift
//  TeeTimeCaddie
//

import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// The Games tab's "New invite" card: every invitation waiting for the player's answer, a row each.
///
/// Tapping a row opens that game. Yes and No answer right there, at once. A full game's Yes is
/// disabled, and its row carries a "Full" chip; No still works. The twin of `InviteCard` on Android.
struct InviteCard: View {
    @EnvironmentObject private var theme: AppTheme

    private let uiState: GamesListUiStateContent
    private let onInviteClick: (String) -> Void
    private let onAnswer: (InviteRowUiState, Bool) -> Void

    /// - Parameters:
    ///   - uiState: The tab's content, for its invitations and their header.
    ///   - onInviteClick: Called with the game's id when a row is tapped.
    ///   - onAnswer: Called with the invitation and whether the answer was Yes.
    init(
        uiState: GamesListUiStateContent,
        onInviteClick: @escaping (String) -> Void,
        onAnswer: @escaping (InviteRowUiState, Bool) -> Void
    ) {
        self.uiState = uiState
        self.onInviteClick = onInviteClick
        self.onAnswer = onAnswer
    }

    var body: some View {
        TtcAccentCard(contentPadding: TtcCardStyle.noContentPadding) {
            VStack(alignment: .leading, spacing: 0) {
                Label {
                    Text(uiState.invitesHeader.localized().uppercased())
                } icon: {
                    Image(SfSymbol.envelopeBadgeFill)
                }
                .font(.caption.weight(.semibold))
                .foregroundStyle(theme.colorScheme.primary)
                .padding(InviteCardStyle.headerPadding)

                ForEach(Array(uiState.invites.enumerated()), id: \.element.id) { index, invite in
                    if index > 0 {
                        Divider()
                            .overlay(theme.colorScheme.outlineVariant)
                            .padding(.leading, InviteCardStyle.leadingPadding)
                    }
                    InviteRow(
                        uiState: invite,
                        onClick: { onInviteClick(invite.id) },
                        onAnswer: { accept in onAnswer(invite, accept) }
                    )
                }
            }
        }
    }
}

fileprivate struct InviteRow: View {
    @EnvironmentObject private var theme: AppTheme

    let uiState: InviteRowUiState
    let onClick: () -> Void
    let onAnswer: (Bool) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: InviteCardStyle.lineSpacing) {
            HStack(spacing: InviteCardStyle.chipSpacing) {
                Text(uiState.course)
                    .font(.headline)
                    .foregroundStyle(theme.colorScheme.onSurface)
                    .lineLimit(1)
                if uiState.isFull {
                    TtcChip(GMR.strings().games_invite_full_chip.localized(), size: .small)
                }
            }
            Label {
                Text(uiState.dateAndTime.localized())
            } icon: {
                Image(SfSymbol.calendar)
            }
            .font(.subheadline)
            .foregroundStyle(theme.colorScheme.onSurfaceVariant)

            HStack(spacing: InviteCardStyle.actionSpacing) {
                TtcPlayerStack {
                    ForEach(Array(uiState.players.enumerated()), id: \.offset) { _, player in
                        TtcPlayerItem {
                            TtcAvatar(player.initial, color: player.tone.toColorRole(), size: TtcAvatarStyle.smallSize)
                        }
                    }
                }
                Spacer(minLength: 0)
                TtcOutlinedButton(GR.strings().no.localized(), dense: true, action: { onAnswer(false) })
                TtcButton(GR.strings().yes.localized(), dense: true, action: { onAnswer(true) })
                    .disabled(uiState.isFull)
            }
            .padding(.top, InviteCardStyle.actionsTopPadding)
        }
        .padding(InviteCardStyle.rowPadding)
        .frame(maxWidth: .infinity, alignment: .leading)
        .contentShape(Rectangle())
        .onTapGesture(perform: onClick)
    }
}

/// Spacing for the invite card — the twin of `InviteCardDefaults` on Android.
fileprivate enum InviteCardStyle {
    /// Content starts this far in from the accent rail, rows and dividers alike.
    static let leadingPadding: CGFloat = 18
    static let headerPadding = EdgeInsets(top: 14, leading: leadingPadding, bottom: 6, trailing: 16)
    static let rowPadding = EdgeInsets(top: 12, leading: leadingPadding, bottom: 14, trailing: 16)
    static let lineSpacing: CGFloat = 2
    static let chipSpacing: CGFloat = 8
    static let actionsTopPadding: CGFloat = 8
    static let actionSpacing: CGFloat = 8
}

// MARK: - Previews

#Preview("One invite") {
    TeeTimeCaddieTheme {
        InviteCard(
            uiState: GamesListUiStateContent(games: [], invites: [InviteRowUiStateKt.previewInviteRowUiState]),
            onInviteClick: { _ in },
            onAnswer: { _, _ in }
        )
        .padding()
    }
}

#Preview("Two invites, one full - Dark") {
    TeeTimeCaddieTheme {
        InviteCard(
            uiState: GamesListUiStateContent(games: [], invites: InviteRowUiStateKt.previewInviteRowUiStateList),
            onInviteClick: { _ in },
            onAnswer: { _, _ in }
        )
        .padding()
    }
    .preferredColorScheme(.dark)
}
