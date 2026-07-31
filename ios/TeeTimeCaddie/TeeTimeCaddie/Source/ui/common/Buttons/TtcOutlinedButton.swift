import SwiftUI
import ThemeUI

/// An outlined pill button from the Fairway Morning design system.
///
/// By default (`.neutral`) it renders the design's neutral treatment: on-surface text with a subtle
/// `outline` border. `.primary`, `.secondary` and `.tertiary` tint the text and border to that role.
/// Use [icon] for an optional leading icon, [dense] for tighter padding, and [isLoading] to show an
/// animated indicator in place of the label (taps are ignored while loading). For a full-width CTA,
/// apply `.frame(maxWidth: .infinity)`.
struct TtcOutlinedButton: View {
    @EnvironmentObject private var theme: AppTheme

    private let title: String
    private let color: TtcButtonColor
    private let icon: ImageResource?
    private let dense: Bool
    private let isLoading: Bool
    private let action: () -> Void

    init(
        _ title: String,
        color: TtcButtonColor = .neutral,
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
        Button(action: { if !isLoading { action() } }) {
            TtcButtonLabel(title: title, icon: icon)
                .loadingOverlay(type: .Flashing, isLoading: isLoading)
        }
        .buttonStyle(TtcPillButtonStyle(
            background: .clear,
            foreground: color.outlinedForeground(scheme),
            disabledBackground: .clear,
            disabledForeground: scheme.onSurfaceVariant,
            border: color.outlinedBorder(scheme),
            disabledBorder: scheme.outlineVariant,
            dense: dense
        ))
    }
}

#Preview("Light") {
    TeeTimeCaddieTheme {
        VStack(alignment: .leading, spacing: 12) {
            TtcOutlinedButton("Invite more") {}
            TtcOutlinedButton("Invite", icon: .Icons.calendarAdd) {}
            TtcOutlinedButton("Primary", color: .primary) {}
            TtcOutlinedButton("Create account") {}.frame(maxWidth: .infinity)
            TtcOutlinedButton("Disabled") {}.disabled(true)
            TtcOutlinedButton("Loading", isLoading: true) {}
        }
        .padding()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        VStack(alignment: .leading, spacing: 12) {
            TtcOutlinedButton("Invite more") {}
            TtcOutlinedButton("Primary", color: .primary) {}
            TtcOutlinedButton("Disabled") {}.disabled(true)
            TtcOutlinedButton("Loading", isLoading: true) {}
        }
        .padding()
    }
    .preferredColorScheme(.dark)
}
