import SwiftUI
import ThemeUI

/// Wraps a single ``TtcAvatar`` in the Fairway Morning stack "separator ring" — a `ringColor` circle 2pt
/// larger than the avatar, so overlapping avatars in a ``TtcPlayerStack`` read as distinct discs. Use it
/// like `Button { Icon() }`:
///
/// ```swift
/// TtcPlayerStack {
///     TtcPlayerItem { TtcAvatar("B", color: .secondary, size: 28) }
///     TtcPlayerItem { TtcAvatar("J", color: .tertiary, size: 28) }
/// }
/// ```
struct TtcPlayerItem<Content: View>: View {
    @EnvironmentObject private var theme: AppTheme

    private let ringColor: Color?
    private let content: Content

    /// - Parameters:
    ///   - ringColor: The separator ring color — the surface the stack sits on. Defaults to
    ///     `surfaceContainer` (the card fill the stack usually lives on).
    ///   - content: The avatar to wrap.
    init(ringColor: Color? = nil, @ViewBuilder content: () -> Content) {
        self.ringColor = ringColor
        self.content = content()
    }

    var body: some View {
        content
            .padding(TtcAvatarStyle.stackRing)
            .background(ringColor ?? theme.colorScheme.surfaceContainer, in: Circle())
    }
}

/// A horizontally overlapping row of ``TtcPlayerItem``s — the Fairway Morning "player stack". It is a
/// thin `HStack` with negative spacing, so its content block reads identically to the Compose version.
///
/// To end the stack with an overflow circle, add a ``TtcPlayerOverflow`` ("+N", more players than the
/// stack shows) or a ``TtcPlayerAddSlot`` ("+", open slots to fill) as the last item.
struct TtcPlayerStack<Content: View>: View {
    private let overlap: CGFloat
    private let content: Content

    init(overlap: CGFloat = TtcAvatarStyle.stackOverlap, @ViewBuilder content: () -> Content) {
        self.overlap = overlap
        self.content = content()
    }

    var body: some View {
        HStack(spacing: -overlap) {
            content
        }
    }
}

/// A "+N" circle that ends a ``TtcPlayerStack``: more players than the stack shows. It wears the same
/// separator ring as a ``TtcPlayerItem`` and the `.neutral` avatar's colors.
struct TtcPlayerOverflow: View {
    private let text: String
    private let size: CGFloat
    private let ringColor: Color?

    /// - Parameters:
    ///   - text: The circle's text, e.g. "+2".
    ///   - size: The circle's diameter; match the stack's avatars.
    ///   - ringColor: The separator ring color, as on ``TtcPlayerItem``.
    init(_ text: String, size: CGFloat = TtcAvatarStyle.smallSize, ringColor: Color? = nil) {
        self.text = text
        self.size = size
        self.ringColor = ringColor
    }

    var body: some View {
        OverflowCircle(size: size, ringColor: ringColor) {
            Text(text)
                .font(.caption.weight(.medium))
                .lineLimit(1)
        }
    }
}

/// A "+" circle that ends a ``TtcPlayerStack``: there are open slots to fill. It wears the same
/// separator ring as a ``TtcPlayerItem`` and the `.neutral` avatar's colors.
///
/// Not the same as a ``TtcAvatarPlaceholder/empty`` avatar, which stands for one empty slot in a list
/// of players and is drawn as a dashed ring.
struct TtcPlayerAddSlot: View {
    private let size: CGFloat
    private let ringColor: Color?

    /// - Parameters:
    ///   - size: The circle's diameter; match the stack's avatars.
    ///   - ringColor: The separator ring color, as on ``TtcPlayerItem``.
    init(size: CGFloat = TtcAvatarStyle.smallSize, ringColor: Color? = nil) {
        self.size = size
        self.ringColor = ringColor
    }

    var body: some View {
        OverflowCircle(size: size, ringColor: ringColor) {
            Image(SfSymbol.plus)
                .font(.system(size: TtcAvatarStyle.overflowIconSize(size), weight: .medium))
        }
    }
}

fileprivate struct OverflowCircle<Content: View>: View {
    @EnvironmentObject private var theme: AppTheme

    let size: CGFloat
    let ringColor: Color?
    @ViewBuilder let content: () -> Content

    var body: some View {
        let colors = TtcAvatarStyle.toneColors(.neutral, theme.colorScheme)
        TtcPlayerItem(ringColor: ringColor) {
            Circle()
                .fill(colors.background)
                .frame(width: size, height: size)
                .overlay {
                    content()
                        .foregroundStyle(colors.foreground)
                }
        }
    }
}

#Preview("Light") {
    TeeTimeCaddieTheme {
        PlayerStackPreviewContent()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        PlayerStackPreviewContent()
    }
    .preferredColorScheme(.dark)
}

private struct PlayerStackPreviewContent: View {
    var body: some View {
        HStack(spacing: 24) {
            TtcPlayerStack {
                TtcPlayerItem { TtcAvatar("B", color: .secondary, size: TtcAvatarStyle.smallSize) }
                TtcPlayerItem { TtcAvatar("J", color: .tertiary, size: TtcAvatarStyle.smallSize) }
                TtcPlayerItem { TtcAvatar("M", color: .primary, size: TtcAvatarStyle.smallSize) }
            }
            TtcPlayerStack {
                TtcPlayerItem { TtcAvatar(placeholder: .guest, size: TtcAvatarStyle.smallSize) }
                TtcPlayerOverflow("+2")
            }
            TtcPlayerStack {
                TtcPlayerItem { TtcAvatar("B", color: .secondary, size: TtcAvatarStyle.smallSize) }
                TtcPlayerAddSlot()
            }
        }
        .padding()
    }
}
