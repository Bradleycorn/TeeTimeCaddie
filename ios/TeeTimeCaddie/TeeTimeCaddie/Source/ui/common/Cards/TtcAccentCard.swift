import SwiftUI
import ThemeUI

/// A card with an accent rail, from the Fairway Morning design system.
///
/// This is the plain ``TtcCard`` with a 4pt solid bar down its leading edge — the design's "New
/// invite" treatment. The rail marks a card as needing attention *without* changing the card's fill,
/// so the fill is always the neutral `surfaceContainer`: there is deliberately no `color` parameter,
/// because a tinted fill carries a different meaning entirely and the two are never combined.
///
/// Use `accent` to choose the signal the rail carries; `.primary` (the default) is the design's
/// shipped treatment. `.neutral` is the one ``TtcColorRole`` with no meaning here — a neutral rail
/// on a neutral fill reads as no rail at all — and falls back to `.primary`; see
/// ``TtcCardStyle/accentColor(_:_:)``.
///
/// Everything else — the fill, the `radius-md` shape and clip, the press feedback, full width — comes from
/// ``TtcCard``, which this wraps. The rail lives inside that clip, so its leading corners are
/// rounded along with the card's.
struct TtcAccentCard<Content: View>: View {
    @EnvironmentObject private var theme: AppTheme

    private let accent: TtcColorRole
    private let contentPadding: EdgeInsets
    private let onClick: (() -> Void)?
    private let content: Content

    init(
        accent: TtcColorRole = .primary,
        contentPadding: EdgeInsets = TtcCardStyle.contentPadding,
        onClick: (() -> Void)? = nil,
        @ViewBuilder content: () -> Content
    ) {
        self.accent = accent
        self.contentPadding = contentPadding
        self.onClick = onClick
        self.content = content()
    }

    var body: some View {
        // The card supplies no padding: the rail must reach the card's edges, so `contentPadding`
        // is applied to the content column below instead.
        TtcCard(
            color: .neutral,
            contentPadding: TtcCardStyle.noContentPadding,
            onClick: onClick
        ) {
            HStack(spacing: 0) {
                // `Rectangle` is greedy vertically, so it fills the HStack's height — which the
                // content column sets. No intrinsic-size machinery needed, unlike Compose.
                Rectangle()
                    .fill(TtcCardStyle.accentColor(accent, theme.colorScheme))
                    .frame(width: TtcCardStyle.accentWidth)

                VStack(alignment: .leading, spacing: 0) { content }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(contentPadding)
            }
        }
    }
}

#Preview("Light") {
    TeeTimeCaddieTheme {
        TtcAccentCardPreviewContent()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        TtcAccentCardPreviewContent()
    }
    .preferredColorScheme(.dark)
}

private struct TtcAccentCardPreviewContent: View {
    @EnvironmentObject private var theme: AppTheme

    var body: some View {
        let scheme = theme.colorScheme
        return ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                TtcAccentCard { Text("Primary rail (default)") }
                TtcAccentCard(accent: .secondary) { Text("Secondary rail") }
                TtcAccentCard(accent: .tertiary) { Text("Tertiary rail") }
                TtcAccentCard(accent: .error) { Text("Error rail") }
                TtcAccentCard(accent: .neutral) { Text("Neutral rail (falls back to Primary)") }
                TtcAccentCard(onClick: {}) { Text("Tappable accent card") }

                // Approximates the design's "New invite" card, to check the rail against
                // multi-line content.
                TtcAccentCard {
                    HStack(spacing: 6) {
                        Image(systemName: "envelope.badge.fill")
                            .font(.system(size: 14))
                        Text("NEW INVITE").font(.caption.weight(.medium))
                    }
                    .foregroundStyle(scheme.primary)

                    Spacer().frame(height: 8)
                    Text("Pebble Beach").font(.title2.weight(.semibold))
                    Spacer().frame(height: 8)
                    Text("Saturday, Oct 12 · 9:00 AM")
                        .font(.subheadline)
                        .foregroundStyle(scheme.onSurfaceVariant)
                }
            }
            .padding()
        }
    }
}
