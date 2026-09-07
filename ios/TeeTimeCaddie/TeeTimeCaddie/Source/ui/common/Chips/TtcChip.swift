import SwiftUI
import ThemeUI

/// A chip from the Fairway Morning design system.
///
/// Renders a compact, `radius-sm`-rounded chip whose *color carries meaning*: `.primary` (green, the
/// organizer), `.secondary` (gold, pending), `.tertiary` (sky, confirmed), `.error` (declined /
/// can't play) and the default `.neutral` (ambient). `.filled` (the default) is the
/// container-filled status/identity badge (e.g. "Confirmed", "Organizer", "3/4 filled"); `.outlined`
/// is the transparent, bordered "suggestion" treatment. It is a static badge by default; pass
/// [onClick] to make it tappable.
struct TtcChip: View {
    @EnvironmentObject private var theme: AppTheme

    private let text: String
    private let color: TtcColorRole
    private let size: TtcChipSize
    private let variant: TtcChipVariant
    private let icon: ImageResource?
    private let onClick: (() -> Void)?

    init(
        _ text: String,
        color: TtcColorRole = .neutral,
        size: TtcChipSize = .medium,
        variant: TtcChipVariant = .filled,
        icon: ImageResource? = nil,
        onClick: (() -> Void)? = nil
    ) {
        self.text = text
        self.color = color
        self.size = size
        self.variant = variant
        self.icon = icon
        self.onClick = onClick
    }

    /// Resolves the background / foreground / border colors for the current [variant] + [color].
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
            text: text,
            icon: icon,
            size: size,
            background: colors.background,
            foreground: colors.foreground,
            border: colors.border,
            shape: shape
        )
        if let onClick {
            Button(action: onClick) { label }
                .buttonStyle(TtcChipButtonStyle(shape: shape))
        } else {
            label
        }
    }
}

#Preview("Chips") {
    TeeTimeCaddieTheme {
        HStack(alignment: .top, spacing: 12) {
            VStack(alignment: .leading, spacing: 8) {
                TtcChip("Primary", color: .primary, icon: .Icons.calendar)
                TtcChip("Secondary", color: .secondary, icon: .Icons.calendar)
                TtcChip("Tertiary", color: .tertiary, icon: .Icons.calendar)
                TtcChip("Declined", color: .error, icon: .Icons.calendar)
                TtcChip("Neutral", icon: .Icons.calendar)
            }
            VStack(alignment: .leading, spacing: 8) {
                TtcChip("Primary Small", color: .primary, size: .small, icon: .Icons.calendar)
                TtcChip("Secondary Small", color: .secondary, size: .small, icon: .Icons.calendar)
                TtcChip("Tertiary Small", color: .tertiary, size: .small, icon: .Icons.calendar)
                TtcChip("Neutral Small", size: .small, icon: .Icons.calendar)
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
                TtcChip("Tappable", color: .primary) {}
                TtcChip("Tappable Outlined", color: .primary, variant: .outlined) {}
            }
        }
        .padding()
    }
}

