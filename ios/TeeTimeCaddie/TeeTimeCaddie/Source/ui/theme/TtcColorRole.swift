import Foundation

/// The semantic color role a Ttc component renders itself in — the app's single tonal vocabulary,
/// shared by every component that offers a choice of color.
///
/// These are the Fairway Morning design system's roles, resolved through the app's `AppTheme`
/// `ThemeColors`. A component names a role; it does **not** name a color.
///
/// ## A role is not a color
///
/// The same role deliberately resolves to different tones in different components, because emphasis
/// is a property of the component, not of the role:
///
/// - A filled ``TtcButton`` paints `.primary` in the **solid** `primary` (it's the screen's CTA),
///   while a ``TtcChip`` and a ``TtcCard`` paint it in the softer `primaryContainer`.
/// - `.neutral` lands on a different rung of the surface ramp per component — `surfaceContainer` for
///   a card, `surfaceContainerHigh` for a chip, `surfaceContainerHighest` for a button — because a
///   chip sits on a card sits on the screen canvas.
///
/// Each component's `Ttc*Style` namespace owns that mapping and documents its own choices. Consult
/// those, not this enum, for what a role looks like in a given component.
///
/// ## Not every role is meaningful everywhere
///
/// Components render every role, so this vocabulary is uniform across the design system. The one
/// exception is ``TtcAccentCard``, whose rail has no `.neutral` treatment — a neutral rail against a
/// neutral card fill reads as no rail at all. ``TtcCardStyle/accentColor(_:_:)`` documents the
/// fallback.
///
/// ## Adding a role
///
/// Add a case here and every resolver stops compiling until it has been considered. That is
/// intentional: role resolvers must never use a `default:` branch, precisely so this enum stays the
/// one place a new role is introduced.
///
/// Do **not** conform this type to `ShapeStyle`. Every current call site relies on implicit member
/// syntax (`color: .primary`), and a `ShapeStyle` conformance would put these cases into the same
/// overload space as SwiftUI's own `Color.primary` / `HierarchicalShapeStyle.primary`.
///
/// The Android twin is `TtcColorRole` in `theme/TtcColorRole.kt`.
enum TtcColorRole {
    /// The brand green. High-emphasis actions, the organizer, the primary signal.
    case primary

    /// The gold. Warnings, pending states, "needs attention but not urgent".
    case secondary

    /// The sky blue. Confirmations, informational accents.
    case tertiary

    /// Destructive actions, failures, and declined states.
    case error

    /// The ambient surface treatment — no tint. The quiet default for containers.
    case neutral
}
