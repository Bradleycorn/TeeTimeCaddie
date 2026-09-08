package net.bradball.teetimecaddie.android.ui.common.chips

import net.bradball.teetimecaddie.android.theme.TtcColorRole

/** The size a [TtcChip] renders at (controls content padding). */
enum class TtcChipSize {
    Medium,
    Small
}

/**
 * The surface treatment a [TtcChip] renders with: [Filled] (a container fill) or [Outlined]
 * (transparent with a 1dp border). Orthogonal to [TtcColorRole] and [TtcChipSize].
 *
 * The iOS twin is `TtcChipVariant` in `ui/common/Chips/TtcChipStyle.swift`.
 */
enum class TtcChipVariant {
    Filled,
    Outlined
}
