package net.bradball.teetimecaddie.android.ui.common.chips

/**
 * The color role a [TtcChip] renders with.
 *
 * A [TtcChipVariant.Filled] chip uses the role's *container* tones (softer than a `TtcButton`):
 * [Primary] is the green "organizer" chip, [Secondary] the gold "pending" chip, [Tertiary] the sky
 * "confirmed" chip, and [Neutral] the ambient surface treatment. A [TtcChipVariant.Outlined] chip
 * tints its text + border to the role, with [Neutral] using the subtle `onSurfaceVariant` /
 * `outlineVariant` "suggestion" treatment.
 *
 * Maps to the Fairway Morning design system roles via the app's Material 3 theme.
 */
enum class TtcChipColor {
    Primary,
    Secondary,
    Tertiary,
    Neutral
}

/** The size a [TtcChip] renders at (controls content padding). */
enum class TtcChipSize {
    Medium,
    Small
}

/**
 * The surface treatment a [TtcChip] renders with: [Filled] (a container fill) or [Outlined]
 * (transparent with a 1dp border). Orthogonal to [TtcChipColor] and [TtcChipSize].
 */
enum class TtcChipVariant {
    Filled,
    Outlined
}
