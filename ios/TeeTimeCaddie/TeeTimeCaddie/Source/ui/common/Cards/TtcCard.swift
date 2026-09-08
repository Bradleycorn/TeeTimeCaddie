import SwiftUI
import ThemeUI

/// A card from the Fairway Morning design system — the base filled container.
///
/// Renders a `radius-md`-rounded, clipped, tonal block with **no border and no shadow**: depth comes from
/// the surface ramp, not elevation. `.neutral` (the default) is the plain `surfaceContainer` card;
/// the tinted ``TtcColorRole`` cases carry the container tones the design's inline banners are built
/// from.
///
/// The card is a *container*, so its content is a `@ViewBuilder` slot laid out in a leading-aligned
/// `VStack` (matching Compose's `ColumnScope`, so the content block reads identically on both
/// platforms) and it is full width by default. `contentPadding` defaults to 16pt for the common case
/// of a plain card; pass ``TtcCardStyle/noContentPadding`` for a "sectioned" card whose children must
/// run edge to edge — a header strip, a full-bleed divider, or list rows that supply their own
/// padding:
///
/// ```swift
/// TtcCard(contentPadding: TtcCardStyle.noContentPadding) {
///     CardHeader("8:00 AM")
///     TtcDivider()
///     PlayerRow("Brad")
/// }
/// ```
///
/// It is a static container by default; pass `onClick` to make the whole card tappable (a game card
/// that navigates to its details, say). There is deliberately no disabled state — the design has
/// none, as with ``TtcChip``.
struct TtcCard<Content: View>: View {
    @EnvironmentObject private var theme: AppTheme

    private let color: TtcColorRole
    private let contentPadding: EdgeInsets
    private let onClick: (() -> Void)?
    private let content: Content

    init(
        color: TtcColorRole = .neutral,
        contentPadding: EdgeInsets = TtcCardStyle.contentPadding,
        onClick: (() -> Void)? = nil,
        @ViewBuilder content: () -> Content
    ) {
        self.color = color
        self.contentPadding = contentPadding
        self.onClick = onClick
        self.content = content()
    }

    /// The card chrome: the content column, inset by `contentPadding`, on the role's container fill,
    /// clipped to the card shape.
    private var surface: some View {
        let colors = TtcCardStyle.colors(color, theme.colorScheme)
        return VStack(alignment: .leading, spacing: 0) { content }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(contentPadding)
            .background(colors.background)
            .foregroundStyle(colors.foreground)
            .clipShape(TtcCardStyle.shape(theme.shapes))
    }

    var body: some View {
        if let onClick {
            Button(action: onClick) { surface }
                .buttonStyle(TtcCardButtonStyle(shape: TtcCardStyle.shape(theme.shapes)))
        } else {
            surface
        }
    }
}

#Preview("Light") {
    TeeTimeCaddieTheme {
        TtcCardPreviewContent()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        TtcCardPreviewContent()
    }
    .preferredColorScheme(.dark)
}

private struct TtcCardPreviewContent: View {
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                TtcCard {
                    Text("Filled card").font(.headline)
                    Text("surfaceContainer, radius-md, no border, no shadow.")
                        .font(.subheadline)
                }
                TtcCard(color: .primary) { Text("Primary container") }
                TtcCard(color: .secondary) { Text("Secondary container") }
                TtcCard(color: .tertiary) { Text("Tertiary container") }
                TtcCard(color: .error) { Text("Error container") }
                TtcCard(onClick: {}) { Text("Tappable card") }

                // A "list card": no card padding, so the header and dividers run edge to edge.
                TtcCard(contentPadding: TtcCardStyle.noContentPadding) {
                    PreviewCardHeader(title: "8:00 AM")
                    PreviewListRow(primary: "Brad", secondary: "Organizer")
                    PreviewRowDivider(inset: 52)
                    PreviewListRow(primary: "Jeff", secondary: "Confirmed")
                }

                // The shape an inline banner will take once its content component exists.
                TtcCard(color: .error) {
                    Text("We don't recognize that email").font(.subheadline.weight(.semibold))
                    // `Text(verbatim:)` — a plain `Text` markdown-parses its key and would
                    // auto-link the email, overriding the card's foreground color.
                    Text(verbatim: "There's no TeeTimeCaddie account for casey@golf.app.")
                        .font(.footnote)
                }
            }
            .padding()
        }
    }
}

// ─── Preview-only stand-ins ──────────────────────────────────────────────────
// The real CardHeader / ListRow / TtcDivider components come in a later pass; these exist only so
// the sectioned preview above can prove the container composes correctly.

private struct PreviewCardHeader: View {
    @EnvironmentObject private var theme: AppTheme
    let title: String

    var body: some View {
        let scheme = theme.colorScheme
        return HStack(spacing: 10) {
            Image(systemName: "clock.fill").foregroundStyle(scheme.primary)
            Text(title).font(.headline)
            Spacer()
        }
        .padding(EdgeInsets(top: 12, leading: 16, bottom: 12, trailing: 16))
        .frame(maxWidth: .infinity)
        .background(scheme.surfaceContainerHigh)
    }
}

private struct PreviewListRow: View {
    @EnvironmentObject private var theme: AppTheme
    let primary: String
    let secondary: String?

    var body: some View {
        let scheme = theme.colorScheme
        return HStack(spacing: 14) {
            Image(systemName: "person.fill").foregroundStyle(scheme.onSurfaceVariant)
            VStack(alignment: .leading, spacing: 1) {
                Text(primary).font(.body)
                if let secondary {
                    Text(secondary).font(.caption).foregroundStyle(scheme.onSurfaceVariant)
                }
            }
            Spacer()
        }
        .padding(EdgeInsets(top: 12, leading: 16, bottom: 12, trailing: 16))
    }
}

private struct PreviewRowDivider: View {
    @EnvironmentObject private var theme: AppTheme
    let inset: CGFloat

    var body: some View {
        Rectangle()
            .fill(theme.colorScheme.outlineVariant)
            .frame(height: 1)
            .padding(.leading, inset)
    }
}
