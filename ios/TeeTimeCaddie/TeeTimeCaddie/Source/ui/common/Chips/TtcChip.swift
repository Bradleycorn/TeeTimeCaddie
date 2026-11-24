import SwiftUI
import ThemeUI

/// A chip from the Fairway Morning design system, holding any `content`.
///
/// Renders a compact, `radius-sm`-rounded chip that lays `content` out in a row, 6pt apart. Its
/// ``TtcChipColorToken`` *carries meaning*: `.primary` (green, the organizer), `.secondary` (gold,
/// pending), `.tertiary` (sky, confirmed), `.error` (declined / can't play), and the surface tokens
/// for ambient chips, with `.high` the default. `.filled` (the default) fills the chip; `.outlined`
/// is the transparent, bordered "suggestion" treatment. It is a static badge by default; pass
/// `onClick` to make it a button.
///
/// For a plain label with an optional icon, use ``init(_:color:size:variant:icon:onClick:)``.
/// The Android twin is `TtcChip` in `ui/common/chips/TtcChip.kt`.
struct TtcChip<Content: View>: View {
    @EnvironmentObject private var theme: AppTheme

    private let color: TtcChipColorToken
    private let size: TtcChipSize
    private let variant: TtcChipVariant
    private let onClick: (() -> Void)?
    private let content: (Color) -> Content

    /// - Parameters:
    ///   - onClick: When non-nil, makes the chip a button and is called on tap.
    ///   - content: The chip's content. It receives the foreground color for `color` and
    ///     `variant`, to tint text and icons with.
    init(
        color: TtcChipColorToken = .high,
        size: TtcChipSize = .medium,
        variant: TtcChipVariant = .filled,
        onClick: (() -> Void)? = nil,
        @ViewBuilder content: @escaping (_ foreground: Color) -> Content
    ) {
        self.color = color
        self.size = size
        self.variant = variant
        self.onClick = onClick
        self.content = content
    }

    /// Resolves the background / foreground / border colors for the current `variant` + `color`.
    /// A `nil` border means a borderless (filled) chip.
    private func resolvedColors(_ scheme: ThemeColors) -> (background: Color, foreground: Color, border: Color?) {
        switch variant {
        case .filled:
            let colors = TtcChipStyle.filledColors(color, scheme)
            return (colors.background, colors.foreground, nil)
        case .outlined:
            let colors = TtcChipStyle.outlinedColors(color, scheme)
            return (.clear, colors.foreground, colors.border)
        }
    }

    var body: some View {
        let colors = resolvedColors(theme.colorScheme)
        let shape = TtcChipStyle.shape(theme.shapes)
        let label = TtcChipLabel(
            size: size,
            background: colors.background,
            foreground: colors.foreground,
            border: colors.border,
            shape: shape
        ) {
            content(colors.foreground)
        }
        if let onClick {
            Button(action: onClick) { label }
                .buttonStyle(TtcChipButtonStyle(shape: shape))
        } else {
            label
        }
    }
}

extension TtcChip where Content == TtcChipText {

    /// A chip with a `text` label and an optional leading `icon`: the status and identity badge
    /// (e.g. "Confirmed", "Organizer", "3/4 filled") or an outlined "suggestion".
    ///
    /// - Parameters:
    ///   - onClick: When non-nil, makes the chip a button and is called on tap.
    init(
        _ text: String,
        color: TtcChipColorToken = .high,
        size: TtcChipSize = .medium,
        variant: TtcChipVariant = .filled,
        icon: ImageSource? = nil,
        onClick: (() -> Void)? = nil
    ) {
        self.init(color: color, size: size, variant: variant, onClick: onClick) { _ in
            TtcChipText(text: text, icon: icon)
        }
    }
}

/// The content of a text ``TtcChip``: a 14pt icon, if any, and a `caption`-weight label. Both take
/// the chip's foreground color from the label around them.
struct TtcChipText: View {
    let text: String
    let icon: ImageSource?

    var body: some View {
        if let icon {
            Image(icon)
                .font(.system(size: TtcChipStyle.iconSize))
        }
        Text(text)
            .font(.caption.weight(.medium))
            .lineLimit(1)
    }
}

#Preview("Chips") {
    TeeTimeCaddieTheme {
        HStack(alignment: .top, spacing: 12) {
            VStack(alignment: .leading, spacing: 8) {
                TtcChip("Primary", color: .primary, icon: .symbol(.calendar))
                TtcChip("Secondary", color: .secondary, icon: .symbol(.calendar))
                TtcChip("Tertiary", color: .tertiary, icon: .symbol(.calendar))
                TtcChip("Declined", color: .error, icon: .symbol(.calendar))
                TtcChip("Neutral", icon: .symbol(.calendar))
                TtcChip("Surface", color: .surface, icon: .symbol(.calendar))
                TtcChip("Highest", color: .highest, icon: .symbol(.calendar))
            }
            VStack(alignment: .leading, spacing: 8) {
                TtcChip("Primary Small", color: .primary, size: .small, icon: .symbol(.calendar))
                TtcChip("Secondary Small", color: .secondary, size: .small, icon: .symbol(.calendar))
                TtcChip("Tertiary Small", color: .tertiary, size: .small, icon:.symbol(.calendar))
                TtcChip("Neutral Small", size: .small, icon: .symbol(.calendar))
            }
        }
        HStack(alignment: .top, spacing: 12) {
            VStack(alignment: .leading, spacing: 8) {
                TtcChip("Primary Outlined", color: .primary, variant: .outlined)
                TtcChip("Secondary Outlined", color: .secondary, variant: .outlined)
                TtcChip("Tertiary Outlined", color: .tertiary, variant: .outlined)
                TtcChip("Declined Outlined", color: .error, variant: .outlined)
                TtcChip("Neutral Outlined", variant: .outlined)
            }
            VStack(alignment: .leading, spacing: 8) {
                TtcChip("Tappable", color: .primary, onClick: {})
                TtcChip("Tappable Outlined", color: .primary, variant: .outlined, onClick: {})
                TtcChip(color: .highest, size: .small, onClick: {}) { foreground in
                    TtcAvatar("S", color: .tertiary, size: 24)
                    Text("Content")
                        .font(.subheadline.weight(.medium))
                    Image(SfSymbol.xmark)
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(foreground)
                }
            }
        }
        .padding()
    }
}

