import SwiftUI

/// Shared sizing and spacing values for ``TtcEmptyState``, derived from the Fairway Morning
/// "Feedback & overlays" spec. (Parallels `TtcCardStyle` / `TtcAvatarStyle`.)
///
/// The medallion is the one place the empty state uses color, and the design ships exactly one
/// treatment for it — the `primaryContainer` / `onPrimaryContainer` pair. ``TtcEmptyState``
/// therefore takes no ``TtcColorRole`` parameter at all: the roles are pinned here because there
/// is nothing for a caller to choose between.
///
/// Typography is not here either. The app uses built-in SwiftUI type (see the iOS `CLAUDE.md`), so
/// the title is `.title2.weight(.semibold)` and the description `.subheadline`, applied at the point
/// of use as they are in `TtcCard` and `TtcAccentCard`.
enum TtcEmptyStateStyle {

    /// Diameter of the icon medallion — the design's 104px circle.
    static let medallionSize: CGFloat = 104

    /// Glyph size inside the medallion — the design's 52px icon.
    static let iconSize: CGFloat = 52

    /// Vertical gap between the empty state's stacked elements — Fairway Morning `space-md`.
    static let spacing: CGFloat = 16

    /// Extra gap below the medallion, on top of ``spacing``. The design gives the medallion its own
    /// `margin-bottom: 4`, letting the art breathe a little more than the text block does.
    static let medallionBottomGap: CGFloat = 4

    /// Horizontal inset around the whole block. The design's container is `padding: 24px 32px 96px`;
    /// only the 32px side inset belongs to the component — the vertical padding (and especially the
    /// 96px bottom, which clears the Games screen's FAB and tab bar) is the caller's screen chrome.
    static let horizontalPadding: CGFloat = 32

    /// Measure cap on the description, so the copy wraps for readability rather than for the screen.
    static let descriptionMaxWidth: CGFloat = 280

    /// Extra gap above `extraContent`, on top of ``spacing`` — the design's `margin-top: 8`.
    static let extraContentTopGap: CGFloat = 8

    /// Gap between multiple views supplied to `extraContent` (the design specimen has one).
    static let extraContentSpacing: CGFloat = 8
}
