import SwiftUI
import ThemeUI

/// A circular avatar from the Fairway Morning "Avatars & players" design system.
///
/// `TtcAvatar` has three flavors, exposed as separate initializers over one shared circle:
/// - **initials** — a `color`-tinted circle with the player's initials;
/// - **photo** — a caller-supplied, already-loaded `Image` clipped to the circle (no color role);
/// - **placeholder** — the dashed `.guest` / `.empty` affordance.
///
/// `size` is a free `CGFloat` (see ``TtcAvatarStyle`` for the named sizes) so the same view serves the
/// stack (28), the photo picker (88), and standalone use.
struct TtcAvatar: View {
    @EnvironmentObject private var theme: AppTheme

    private enum Content {
        case initials(String, TtcColorRole)
        case photo(Image)
        case placeholder(TtcAvatarPlaceholder)
    }

    private let content: Content
    private let size: CGFloat

    /// An initials avatar tinted by `color`.
    init(_ initials: String, color: TtcColorRole = .primary, size: CGFloat = TtcAvatarStyle.mediumSize) {
        self.content = .initials(initials, color)
        self.size = size
    }

    /// A photo avatar — the supplied image cropped to fill the circle. No color role.
    init(photo: Image, size: CGFloat = TtcAvatarStyle.mediumSize) {
        self.content = .photo(photo)
        self.size = size
    }

    /// A dashed placeholder avatar (`.guest` person / `.empty` add). Ignores the color role.
    init(placeholder: TtcAvatarPlaceholder, size: CGFloat = TtcAvatarStyle.mediumSize) {
        self.content = .placeholder(placeholder)
        self.size = size
    }

    var body: some View {
        avatarBody(theme.colorScheme)
            .frame(width: size, height: size)
    }

    @ViewBuilder
    private func avatarBody(_ scheme: ThemeColors) -> some View {
        switch content {
        case let .initials(initials, color):
            TtcAvatarInitials(initials: initials, colors: TtcAvatarStyle.toneColors(color, scheme), size: size)
        case let .photo(image):
            image
                .resizable()
                .scaledToFill()
                .frame(width: size, height: size)
                .clipShape(Circle())
        case let .placeholder(placeholder):
            TtcAvatarPlaceholderView(placeholder: placeholder, scheme: scheme, size: size)
        }
    }
}

fileprivate struct TtcAvatarInitials: View {
    let initials: String
    let colors: (background: Color, foreground: Color)
    let size: CGFloat

    var body: some View {
        Circle()
            .fill(colors.background)
            .overlay {
                Text(initials)
                    .font(.system(size: TtcAvatarStyle.textSize(size), weight: .semibold))
                    .foregroundStyle(colors.foreground)
            }
    }
}

fileprivate struct TtcAvatarPlaceholderView: View {
    let placeholder: TtcAvatarPlaceholder
    let scheme: ThemeColors
    let size: CGFloat

    var body: some View {
        Circle()
            .strokeBorder(scheme.outlineVariant, style: TtcAvatarStyle.dashedStroke())
            .overlay {
                Image(systemName: TtcAvatarStyle.placeholderSymbol(placeholder))
                    .font(.system(size: TtcAvatarStyle.iconSize(size)))
                    .foregroundStyle(scheme.onSurfaceVariant)
            }
    }
}

#Preview("Light") {
    TeeTimeCaddieTheme {
        AvatarPreviewContent()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        AvatarPreviewContent()
    }
    .preferredColorScheme(.dark)
}

private struct AvatarPreviewContent: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack(spacing: 12) {
                TtcAvatar("B", color: .secondary)
                TtcAvatar("M", color: .primary)
                TtcAvatar("J", color: .tertiary)
                TtcAvatar("C", color: .error)
                TtcAvatar("K", color: .neutral)
                TtcAvatar(placeholder: .guest)
                TtcAvatar(placeholder: .empty)
            }
            HStack(spacing: 12) {
                TtcAvatar("D", color: .primary, size: TtcAvatarStyle.smallSize)
                TtcAvatar("D", color: .primary, size: TtcAvatarStyle.mediumSize)
                TtcAvatar("D", color: .primary, size: TtcAvatarStyle.largeSize)
            }
        }
        .padding()
    }
}
