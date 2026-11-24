package net.bradball.teetimecaddie.android.ui.common.chips

/** The size a [TtcChip] renders at (controls content padding). */
enum class TtcChipSize {
    Medium,
    Small
}

/**
 * The surface treatment a [TtcChip] renders with: [Filled] (a container fill) or [Outlined]
 * (transparent with a 1dp border). Orthogonal to [TtcChipColorToken] and [TtcChipSize].
 *
 * The iOS twin is `TtcChipVariant` in `ui/common/Chips/TtcChipStyle.swift`.
 */
enum class TtcChipVariant {
    Filled,
    Outlined
}

/**
 * The color a [TtcChip] renders in. The tinted tokens carry meaning; the surface tokens name a
 * `surfaceContainer*` rung, for chips that only need to stand off what's behind them.
 *
 * The iOS twin is `TtcChipColorToken` in `ui/common/Chips/TtcChipStyle.swift`.
 */
enum class TtcChipColorToken {
    /** Green, the organizer. */
    Primary,

    /** Gold, pending. */
    Secondary,

    /** Sky, confirmed. */
    Tertiary,

    /** Declined or can't play. */
    Error,

    /** `surfaceContainer`. */
    Surface,

    /** `surfaceContainerHigh`, the default: one rung above a card. */
    High,

    /** `surfaceContainerHighest`. */
    Highest,

    /** `surfaceContainerLow`. */
    Low,

    /** `surfaceContainerLowest`. */
    Lowest
}
