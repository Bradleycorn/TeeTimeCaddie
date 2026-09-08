import SwiftUI
import ThemeUI

/// A filled pill button from the Fairway Morning design system.
///
/// `.primary` (the default) renders the solid primary "green" CTA and `.error` the solid
/// destructive one ("Delete tee time", "Leave game"); the remaining ``TtcColorRole`` cases render
/// the "tonal" look (a container fill). Use [icon] for an optional leading icon, [dense] for
/// tighter padding, and [isLoading] to show an animated indicator in place of the label (taps are
/// ignored while loading). For a full-width CTA, apply `.frame(maxWidth: .infinity)`.
struct TtcButton: View {
    @EnvironmentObject private var theme: AppTheme

    private let title: String
    private let color: TtcColorRole
    private let icon: ImageResource?
    private let dense: Bool
    private let isLoading: Bool
    private let action: () -> Void

    init(
        _ title: String,
        color: TtcColorRole = .primary,
        icon: ImageResource? = nil,
        dense: Bool = false,
        isLoading: Bool = false,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.color = color
        self.icon = icon
        self.dense = dense
        self.isLoading = isLoading
        self.action = action
    }

    var body: some View {
        let scheme = theme.colorScheme
        let colors = TtcButtonStyle.filledColors(color, scheme)
        Button(action: { if !isLoading { action() } }) {
            TtcButtonLabel(title: title, icon: icon)
                .loadingOverlay(type: .Flashing, isLoading: isLoading)
        }
        .buttonStyle(TtcPillButtonStyle(
            background: colors.background,
            foreground: colors.foreground,
            disabledBackground: scheme.surfaceContainerHighest,
            disabledForeground: scheme.onSurfaceVariant,
            border: nil,
            disabledBorder: nil,
            dense: dense
        ))
    }
}

#Preview("Light") {
    TeeTimeCaddieTheme {
        VStack(alignment: .leading, spacing: 12) {
            TtcButton("Book") {}
            TtcButton("Manage", color: .secondary) {}
            TtcButton("Delete tee time", color: .error) {}
            TtcButton("Add tee time", icon: .Icons.calendarAdd) {}
            TtcButton("New", icon: .Icons.calendarAdd, dense: true) {}
            TtcButton("Sign in") {}.frame(maxWidth: .infinity)
            TtcButton("Disabled") {}.disabled(true)
            TtcButton("Loading", isLoading: true) {}
        }
        .padding()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        VStack(alignment: .leading, spacing: 12) {
            TtcButton("Book") {}
            TtcButton("Manage", color: .secondary) {}
            TtcButton("Delete tee time", color: .error) {}
            TtcButton("Add tee time", icon: .Icons.calendarAdd) {}
            TtcButton("Disabled") {}.disabled(true)
            TtcButton("Loading", isLoading: true) {}
        }
        .padding()
    }
    .preferredColorScheme(.dark)
}
