import SwiftUI
import ThemeUI

/// A chip from the Fairway Morning design system.
///
/// Renders a compact, 8dp-rounded pill whose *color carries meaning*: `.primary` (green), `.secondary`
/// (gold), `.tertiary` (sky) and the default `.neutral` (ambient). `.filled` (the default) is the
/// container-filled status/identity badge (e.g. "Confirmed", "Organizer", "3/4 filled"); `.outlined`
/// is the transparent, bordered "suggestion" treatment. It is a static badge by default; pass
/// [onClick] to make it tappable.
struct TtcChip: View {
    @EnvironmentObject private var theme: AppTheme

    private let text: String
    private let color: TtcChipColor
    private let size: TtcChipSize
    private let variant: TtcChipVariant
    private let icon: ImageResource?
    private let onClick: (() -> Void)?

    init(
        _ text: String,
        color: TtcChipColor = .neutral,
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
            let colors = color.filledColors(scheme)
            return (colors.background, colors.foreground, nil)
        case .outlined:
            let colors = color.outlinedColors(scheme)
            return (.clear, colors.foreground, colors.border)
        }
    }

    var body: some View {
        let colors = resolvedColors(theme.colorScheme)
        let label = TtcChipLabel(
            text: text,
            icon: icon,
            size: size,
            background: colors.background,
            foreground: colors.foreground,
            border: colors.border
        )
        if let onClick {
            Button(action: onClick) { label }
                .buttonStyle(TtcChipButtonStyle())
        } else {
            label
        }
    }
}

#Preview("Light") {
    TeeTimeCaddieTheme {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 8) {
                TtcChip("Confirmed", color: .tertiary, icon: .Icons.calendar)
                TtcChip("Pending", color: .secondary, icon: .Icons.calendar)
                TtcChip("Organizer", color: .primary, icon: .Icons.calendar)
                TtcChip("3/4 filled")
            }
            HStack(spacing: 8) {
                TtcChip("Confirmed", color: .tertiary, size: .small)
                TtcChip("Pending", color: .secondary, size: .small)
                TtcChip("1/4 filled", size: .small)
            }
            HStack(spacing: 8) {
                TtcChip("Pebble Beach", variant: .outlined)
                TtcChip("Bethpage Black", variant: .outlined)
                TtcChip("Torrey Pines", variant: .outlined)
            }
            HStack(spacing: 8) {
                TtcChip("Primary", color: .primary, variant: .outlined)
                TtcChip("Secondary", color: .secondary, variant: .outlined)
                TtcChip("Tertiary", color: .tertiary, variant: .outlined)
            }
            TtcChip("Tappable", color: .primary) {}
        }
        .padding()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 8) {
                TtcChip("Confirmed", color: .tertiary, icon: .Icons.calendar)
                TtcChip("Pending", color: .secondary, icon: .Icons.calendar)
                TtcChip("Organizer", color: .primary, icon: .Icons.calendar)
                TtcChip("3/4 filled")
            }
            TtcChip("Tappable", color: .primary) {}
        }
        .padding()
    }
    .preferredColorScheme(.dark)
}
