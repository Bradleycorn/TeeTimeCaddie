package net.bradball.teetimecaddie.android.theme

import androidx.compose.material3.MaterialTheme

/**
 * The semantic color role a Ttc component renders itself in — the app's single tonal vocabulary,
 * shared by every component that offers a choice of color.
 *
 * These are the Fairway Morning design system's roles, resolved through the app's Material 3
 * [MaterialTheme] color scheme. A component names a role; it does **not** name a color.
 *
 * ## A role is not a color
 *
 * The same role deliberately resolves to different tones in different components, because emphasis
 * is a property of the component, not of the role:
 *
 * - A filled `TtcButton` paints [Primary] in the **solid** `primary` (it's the screen's CTA), while
 *   a `TtcChip` and a `TtcCard` paint it in the softer `primaryContainer`.
 * - [Neutral] lands on a different rung of the surface ramp per component — `surfaceContainer` for
 *   a card, `surfaceContainerHigh` for a chip, `surfaceContainerHighest` for a button — because a
 *   chip sits on a card sits on the screen canvas.
 *
 * Each component's `Ttc*Defaults` object owns that mapping and documents its own choices. Consult
 * those, not this enum, for what a role looks like in a given component.
 *
 * ## Not every role is meaningful everywhere
 *
 * Components render every role, so this vocabulary is uniform across the design system. The one
 * exception is `TtcAccentCard`, whose rail has no [Neutral] treatment — a neutral rail against a
 * neutral card fill reads as no rail at all. `TtcCardDefaults.accentColor` documents the fallback.
 *
 * ## Adding a role
 *
 * Add a case here and every resolver stops compiling until it has been considered. That is
 * intentional: role resolvers must never use an `else` branch, precisely so this enum stays the one
 * place a new role is introduced.
 *
 * The iOS twin is `TtcColorRole` in `ui/theme/TtcColorRole.swift`.
 */
enum class TtcColorRole {
    /** The brand green. High-emphasis actions, the organizer, the primary signal. */
    Primary,

    /** The gold. Warnings, pending states, "needs attention but not urgent". */
    Secondary,

    /** The sky blue. Confirmations, informational accents. */
    Tertiary,

    /** Destructive actions, failures, and declined states. */
    Error,

    /** The ambient surface treatment — no tint. The quiet default for containers. */
    Neutral
}
