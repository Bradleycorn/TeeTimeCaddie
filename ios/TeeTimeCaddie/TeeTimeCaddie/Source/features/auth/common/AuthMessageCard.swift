//
//  AuthMessageCard.swift
//  TeeTimeCaddie
//

import SwiftUI
import ThemeUI

/// Renders an ``AuthMessage`` as an inline block on an auth screen.
///
/// A composition of existing components rather than a new one: a ``TtcCard`` in the message's own
/// role holding an icon, a title, a body and — only when the message has an action — a dense
/// ``TtcOutlinedButton`` in that same role. The Android twin is `AuthMessageBlock`.
struct AuthMessageCard: View {
    @EnvironmentObject private var theme: AppTheme

    let message: AuthMessage
    let onAction: () -> Void

    var body: some View {
        TtcCard(color: message.role) {
            // TtcCard lays its content out in a VStack(spacing: 0), so the block supplies its own.
            VStack(alignment: .leading, spacing: Metrics.actionSpacing) {
                HStack(alignment: .top, spacing: Metrics.iconSpacing) {
                    Icon(message.icon, size: Metrics.iconSize)

                    VStack(alignment: .leading, spacing: Metrics.textSpacing) {
                        Text(message.title)
                            .font(.subheadline.weight(.semibold))

                        // `verbatim` on purpose. A plain `Text` treats its contents as a localization
                        // key and markdown-parses it, which auto-links the interpolated email —
                        // overriding the card's foreground colour with the link tint.
                        Text(verbatim: message.body)
                            .font(.subheadline)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                }

                if let actionTitle = message.actionTitle {
                    TtcOutlinedButton(
                        actionTitle,
                        color: message.role,
                        dense: true,
                        action: onAction
                    )
                }
            }
        }
    }

    private enum Metrics {
        static let iconSize: CGFloat = 24
        static let iconSpacing: CGFloat = 12
        static let textSpacing: CGFloat = 4
        static let actionSpacing: CGFloat = 12
    }
}

// MARK: - Previews

#Preview("Light") {
    TeeTimeCaddieTheme {
        AuthMessageCardPreviews()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        AuthMessageCardPreviews()
    }
    .preferredColorScheme(.dark)
}

fileprivate struct AuthMessageCardPreviews: View {
    var body: some View {
        VStack(spacing: 12) {
            AuthMessageCard(message: .signInFailed, onAction: {})
            AuthMessageCard(message: .emailInUse(email: "dana@example.com"), onAction: {})
            AuthMessageCard(message: .phoneInUse, onAction: {})
        }
        .padding()
    }
}
