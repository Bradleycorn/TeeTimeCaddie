import SwiftUI

/// An avatar for a photo that has to be fetched.
///
/// Separate from ``TtcAvatar`` so that component stays free of I/O: it takes an already-loaded
/// `Image`, which is what lets every avatar preview in the app draw instantly instead of reaching
/// for the network.
///
/// While loading, and on failure, it falls back to the initials disc — so a broken or slow photo
/// URL degrades to a letter rather than to a hole. The Android twin is `rememberTtcImagePainter`,
/// which returns nil until a load succeeds for the same reason.
struct TtcRemoteAvatar: View {
    private let url: URL?
    private let initials: String
    private let color: TtcColorRole
    private let size: CGFloat

    init(
        url: URL?,
        initials: String,
        color: TtcColorRole = .primary,
        size: CGFloat = TtcAvatarStyle.mediumSize
    ) {
        self.url = url
        self.initials = initials
        self.color = color
        self.size = size
    }

    /// Convenience for the common case of a photo URL held as a `String`.
    init(
        urlString: String?,
        initials: String,
        color: TtcColorRole = .primary,
        size: CGFloat = TtcAvatarStyle.mediumSize
    ) {
        self.init(
            url: urlString.flatMap(URL.init(string:)),
            initials: initials,
            color: color,
            size: size
        )
    }

    var body: some View {
        if let url {
            AsyncImage(url: url) { image in
                TtcAvatar(photo: image, size: size)
            } placeholder: {
                fallback
            }
        } else {
            fallback
        }
    }

    private var fallback: some View {
        TtcAvatar(initials, color: color, size: size)
    }
}

// MARK: - Previews

#Preview("Light") {
    TeeTimeCaddieTheme {
        // No URL: the initials fallback, which is also what a failed load shows. Previews
        // deliberately never exercise the network path.
        HStack(spacing: 24) {
            TtcRemoteAvatar(urlString: nil, initials: "D")
            TtcRemoteAvatar(
                urlString: nil,
                initials: "B",
                color: .secondary,
                size: TtcAvatarStyle.pickerSize
            )
        }
        .padding()
    }
}
