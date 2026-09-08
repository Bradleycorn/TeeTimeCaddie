import SwiftUI
import ThemeUI

/// An empty state from the Fairway Morning "Feedback & overlays" design system.
///
/// A centered column of up to four parts: an optional `icon` in a 104pt `primaryContainer`
/// "medallion", the `title`, an optional `description`, and an optional `extraContent` slot for the
/// action that gets the user out of the empty state.
///
/// `TtcEmptyState` renders only the **content column**, at its intrinsic height. Filling and
/// centering the available space is the caller's job, because that is also where clearance for
/// screen chrome (a tab bar, a floating action button) belongs:
///
/// ```swift
/// TtcEmptyState(
///     title: TTR.strings().empty_tee_times_title.desc().localized(),
///     icon: .teeEmpty,
///     description: TTR.strings().empty_tee_times_message.desc().localized()
/// ) {
///     TtcButton("Add a tee time", icon: .Icons.calendarAdd) { onAdd() }
/// }
/// .frame(maxWidth: .infinity, maxHeight: .infinity)
/// ```
///
/// Omit `extraContent` entirely — via the `ExtraContent == EmptyView` initializer — rather than
/// passing an empty block, so the gap above it collapses.
///
/// The medallion has a single treatment in the design, so there is no color role parameter — see
/// ``TtcEmptyStateStyle``.
struct TtcEmptyState<ExtraContent: View>: View {
    @EnvironmentObject private var theme: AppTheme

    private let title: String
    private let icon: ImageResource?
    private let description: String?
    private let extraContent: ExtraContent

    /// An empty state with trailing content — usually the recovery action.
    init(
        title: String,
        icon: ImageResource? = nil,
        description: String? = nil,
        @ViewBuilder extraContent: () -> ExtraContent
    ) {
        self.title = title
        self.icon = icon
        self.description = description
        self.extraContent = extraContent()
    }

    /// True when the caller supplied real trailing content, so the gap above it should be spent.
    private var hasExtraContent: Bool { ExtraContent.self != EmptyView.self }

    var body: some View {
        let scheme = theme.colorScheme
        return VStack(spacing: TtcEmptyStateStyle.spacing) {
            if let icon {
                TtcEmptyStateMedallion(icon: icon, scheme: scheme)
            }

            // `title` and `description` are `String`s, not `LocalizedStringKey`s, so `Text` takes
            // them verbatim — no markdown parsing, no auto-linked emails overriding the color.
            Text(title)
                .font(.title2.weight(.semibold))
                .foregroundStyle(scheme.onSurface)

            if let description {
                Text(description)
                    .font(.subheadline)
                    .foregroundStyle(scheme.onSurfaceVariant)
                    .frame(maxWidth: TtcEmptyStateStyle.descriptionMaxWidth)
            }

            if hasExtraContent {
                VStack(spacing: TtcEmptyStateStyle.extraContentSpacing) { extraContent }
                    .padding(.top, TtcEmptyStateStyle.extraContentTopGap)
            }
        }
        .multilineTextAlignment(.center)
        .padding(.horizontal, TtcEmptyStateStyle.horizontalPadding)
    }
}

extension TtcEmptyState where ExtraContent == EmptyView {
    /// An empty state with no trailing action.
    init(title: String, icon: ImageResource? = nil, description: String? = nil) {
        self.init(title: title, icon: icon, description: description) { EmptyView() }
    }
}

fileprivate struct TtcEmptyStateMedallion: View {
    let icon: ImageResource
    let scheme: ThemeColors

    var body: some View {
        Circle()
            .fill(scheme.primaryContainer)
            .frame(width: TtcEmptyStateStyle.medallionSize, height: TtcEmptyStateStyle.medallionSize)
            .overlay {
                Image(icon)
                    .resizable()
                    .scaledToFit()
                    .frame(width: TtcEmptyStateStyle.iconSize, height: TtcEmptyStateStyle.iconSize)
                    .foregroundStyle(scheme.onPrimaryContainer)
            }
            // The extra gap sits outside the circle, so it doesn't eat into the fill.
            .padding(.bottom, TtcEmptyStateStyle.medallionBottomGap)
            // Decorative — the title carries the state's meaning.
            .accessibilityHidden(true)
    }
}

#Preview("Light") {
    TeeTimeCaddieTheme {
        TtcEmptyStatePreviewContent()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        TtcEmptyStatePreviewContent()
    }
    .preferredColorScheme(.dark)
}

#Preview("Variants") {
    TeeTimeCaddieTheme {
        TtcEmptyStateVariantsPreviewContent()
    }
}

private struct TtcEmptyStatePreviewContent: View {
    var body: some View {
        // The design's composition: the empty state centered in the whole content area.
        TtcEmptyState(
            title: "No games yet",
            icon: .teeEmpty,
            description: "Booked a tee time? Turn it into a game so your foursome lives somewhere "
                + "other than a text thread."
        ) {
            TtcButton("Create a game", icon: .Icons.calendarAdd) {}
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

private struct TtcEmptyStateVariantsPreviewContent: View {
    var body: some View {
        VStack(spacing: 48) {
            // Text only — the gaps left by the medallion and the action collapse.
            TtcEmptyState(
                title: "Tee sheet is wide open",
                description: "You don't have any scheduled tee times."
            )
            // Medallion + title, no supporting copy.
            TtcEmptyState(title: "Nothing on the calendar", icon: .Icons.calendar)
        }
        .padding(.vertical, 32)
    }
}
