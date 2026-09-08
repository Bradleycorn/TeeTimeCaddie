package net.bradball.teetimecaddie.android.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * The app's corner shape scale — the single radius vocabulary every Ttc component draws from,
 * resolved through Material 3's [Shapes] and read as `MaterialTheme.shapes.*`.
 *
 * These are the Fairway Morning `radius-*` tokens. A component names a size; it does **not** name a
 * radius. Each component's `Ttc*Defaults` object owns the mapping and documents its choice:
 *
 * - [Shapes.extraSmall] (`radius-xs`) — text fields, which round their top corners only.
 * - [Shapes.small] (`radius-sm`) — chips.
 * - [Shapes.medium] (`radius-md`) — cards.
 * - [Shapes.large] / [Shapes.extraLarge] — larger surfaces; `extraLarge` is what the Material date
 *   and time picker dialogs already resolve to, so those keep delegating to their own defaults.
 *
 * Note these values match Material 3's own defaults exactly, so passing them to `MaterialTheme`
 * changes nothing today. They are declared anyway: it keeps the scale reviewable beside the iOS twin,
 * and it pins the app's radii to the design rather than to whatever a library ships next. That is not
 * hypothetical — the iOS side inherited a 3-slot 8/10/12 scale until ThemeUI 1.0.2, which put the
 * wrong radius in the semantically correct slot for a card.
 *
 * Buttons are deliberately absent: the design's pill is `CircleShape`, not a radius. See
 * `TtcButtonDefaults.Shape`.
 *
 * The iOS twin is `ttcShapes` in `ui/theme/TeeTimeCaddieTheme.swift`.
 */
val TtcShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
