import SwiftUI
import ThemeUI

/// The Fairway Morning profile photo picker — a large circular affordance for seeding a profile photo.
///
/// It is **presentational**: it renders the current state and fires `onClick` when tapped; launching the
/// OS photo picker and handling the chosen image is the caller's job. When a `photo` or `initials` is
/// supplied it reuses ``TtcAvatar`` for the disc and shows an *edit* badge; otherwise it shows the empty
/// "add a photo" state with an *add* badge.
///
/// An optional `caption` sits beneath the disc and is combined with it for VoiceOver, so the control
/// reads as one thing rather than a button followed by stray text.
struct TtcPhotoPicker: View {
    @EnvironmentObject private var theme: AppTheme

    private let initials: String?
    private let color: TtcColorRole
    private let photo: Image?
    private let size: CGFloat
    private let caption: String?
    private let onClick: () -> Void

    init(
        initials: String? = nil,
        color: TtcColorRole = .primary,
        photo: Image? = nil,
        size: CGFloat = TtcAvatarStyle.pickerSize,
        caption: String? = nil,
        onClick: @escaping () -> Void
    ) {
        self.initials = initials
        self.color = color
        self.photo = photo
        self.size = size
        self.caption = caption
        self.onClick = onClick
    }

    private var hasContent: Bool {
        photo != nil || !(initials ?? "").isEmpty
    }

    var body: some View {
        VStack(spacing: TtcAvatarStyle.captionSpacing) {
            Button(action: onClick) {
                disc
                    .overlay(alignment: .bottomTrailing) { badge }
            }
            .buttonStyle(.plain)

            // Part of the button's label rather than a sibling, for accessibility: "Add a photo
            // (optional)" is what the control does, so VoiceOver should read the two as one.
            if let caption {
                Text(caption)
                    .font(.footnote)
                    .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                    .multilineTextAlignment(.center)
            }
        }
        .accessibilityElement(children: .combine)
    }

    @ViewBuilder
    private var disc: some View {
        if let photo {
            TtcAvatar(photo: photo, size: size)
        } else if let initials, !initials.isEmpty {
            TtcAvatar(initials, color: color, size: size)
        } else {
            emptyDisc
        }
    }

    private var emptyDisc: some View {
        let scheme = theme.colorScheme
        return Circle()
            .fill(scheme.surfaceContainerHigh)
            .overlay { Circle().strokeBorder(scheme.outline, style: TtcAvatarStyle.dashedStroke()) }
            .overlay {
                Image(.symbol(.camera))
                    .font(.system(size: size * 0.3))
                    .foregroundStyle(scheme.onSurfaceVariant)
            }
            .frame(width: size, height: size)
    }

    private var badge: some View {
        let scheme = theme.colorScheme
        let badgeSize = size * 0.32
        return Image(.symbol(hasContent ? .pencil : .plus))
            .font(.system(size: badgeSize * 0.55, weight: .semibold))
            .foregroundStyle(scheme.onPrimary)
            .frame(width: badgeSize, height: badgeSize)
            .background(scheme.primary, in: Circle())
            .padding(3)
            .background(scheme.surface, in: Circle())
    }
}

#Preview("Light") {
    TeeTimeCaddieTheme {
        HStack(spacing: 24) {
            TtcPhotoPicker(caption: "Add a photo (optional)", onClick: {})
            TtcPhotoPicker(
                initials: "B",
                color: .secondary,
                caption: "Photo added · tap to change",
                onClick: {}
            )
        }
        .padding()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        HStack(spacing: 24) {
            TtcPhotoPicker(caption: "Add a photo (optional)", onClick: {})
            TtcPhotoPicker(
                initials: "B",
                color: .secondary,
                caption: "Photo added · tap to change",
                onClick: {}
            )
        }
        .padding()
    }
    .preferredColorScheme(.dark)
}
