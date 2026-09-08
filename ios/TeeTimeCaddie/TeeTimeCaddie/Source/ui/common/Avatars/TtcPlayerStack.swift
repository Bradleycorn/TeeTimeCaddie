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
/// Overflow (`+N`) and an add slot are not part of this component.
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
                TtcPlayerItem { TtcAvatar("B", color: .secondary, size: TtcAvatarStyle.smallSize) }
                TtcPlayerItem { TtcAvatar(placeholder: .guest, size: TtcAvatarStyle.smallSize) }
            }
        }
        .padding()
    }
}
