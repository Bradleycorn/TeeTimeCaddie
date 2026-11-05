//
//  RsvpCard.swift
//  TeeTimeCaddie
//

import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// The invitee's answer to their invitation, under the game's title.
///
/// Before they answer, an accented card asks, with Yes and No; once every slot is taken, it says so
/// and Yes is disabled. After they answer, it collapses to a single row: "You're in" or "You
/// declined". The twin of `RsvpCard` on Android.
struct RsvpCard: View {
    private let uiState: any RsvpUiState
    private let onAnswer: (Bool) -> Void

    /// - Parameter onAnswer: Called with whether the answer was Yes.
    init(uiState: any RsvpUiState, onAnswer: @escaping (Bool) -> Void) {
        self.uiState = uiState
        self.onAnswer = onAnswer
    }

    var body: some View {
        switch onEnum(of: uiState) {
        case .pending(let pending):
            PendingRsvp(uiState: pending, onAnswer: onAnswer)
        case .accepted(let accepted):
            AnsweredRsvp(symbol: .checkmarkCircleFill, message: accepted.message, accepted: true)
        case .declined(let declined):
            AnsweredRsvp(symbol: .xmarkCircleFill, message: declined.message, accepted: false)
        }
    }
}

fileprivate struct PendingRsvp: View {
    @EnvironmentObject private var theme: AppTheme

    let uiState: RsvpUiStatePending
    let onAnswer: (Bool) -> Void

    var body: some View {
        TtcAccentCard(contentPadding: RsvpCardStyle.pendingPadding) {
            VStack(alignment: .leading, spacing: 0) {
                if let title = uiState.title {
                    Text(title.localized())
                        .font(.headline)
                        .foregroundStyle(theme.colorScheme.onSurface)
                }
                Text(uiState.prompt.localized())
                    .font(.subheadline)
                    .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                    .padding(.top, RsvpCardStyle.promptTopPadding)
                HStack(spacing: RsvpCardStyle.buttonSpacing) {
                    TtcButton(GR.strings().yes.localized(), fullWidth: true, action: { onAnswer(true) })
                        .disabled(!uiState.canAccept)
                    TtcOutlinedButton(GR.strings().no.localized(), fullWidth: true, action: { onAnswer(false) })
                }
                .padding(.top, RsvpCardStyle.buttonsTopPadding)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

fileprivate struct AnsweredRsvp: View {
    @EnvironmentObject private var theme: AppTheme

    let symbol: SfSymbol
    let message: LocalizedText
    let accepted: Bool

    var body: some View {
        TtcCard(contentPadding: RsvpCardStyle.answeredPadding) {
            HStack(spacing: RsvpCardStyle.answeredSpacing) {
                Image(symbol)
                    .font(.title2)
                    .foregroundStyle(accepted ? theme.colorScheme.primary : theme.colorScheme.onSurfaceVariant)
                Text(message.localized())
                    .font(.headline)
                    .foregroundStyle(theme.colorScheme.onSurface)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

/// Spacing for the RSVP card — the twin of `RsvpCardDefaults` on Android.
fileprivate enum RsvpCardStyle {
    /// Starts this far in from the accent rail, like the Games tab's invite card.
    static let pendingPadding = EdgeInsets(top: 16, leading: 18, bottom: 16, trailing: 16)
    static let promptTopPadding: CGFloat = 4
    static let buttonsTopPadding: CGFloat = 16
    static let buttonSpacing: CGFloat = 8
    static let answeredPadding = EdgeInsets(top: 12, leading: 16, bottom: 12, trailing: 8)
    static let answeredSpacing: CGFloat = 12
}

// MARK: - Previews

#Preview("States") {
    TeeTimeCaddieTheme {
        VStack(spacing: 16) {
            ForEach(
                Array([
                    GameUiStateKt.previewInvitedGameUiState.rsvp,
                    GameUiStateKt.previewFullGameUiState.rsvp,
                    GameUiStateKt.previewAcceptedGameUiState.rsvp,
                    RsvpUiStateDeclined.shared,
                ].compactMap { $0 }.enumerated()),
                id: \.offset
            ) { _, rsvp in
                RsvpCard(uiState: rsvp, onAnswer: { _ in })
            }
        }
        .padding()
    }
}
