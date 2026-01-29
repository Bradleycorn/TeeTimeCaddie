package net.bradball.teetimecaddie.android.ui.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Marker interface for all navigation destinations in the TeeTime Caddie app.
 *
 * `TtcNavKey` extends the androidx.navigation3 [NavKey] interface and serves as the base type
 * for all navigation destinations in the app. By implementing this interface, destinations
 * become compatible with the [Navigator] and can be used with [NavDisplay].
 *
 * ## Purpose
 *
 * This interface exists to:
 * - Provide a common type for all app navigation destinations
 * - Allow for future app-wide navigation enhancements (e.g., feature toggles, analytics)
 * - Enable type-safe navigation throughout the app
 *
 * ## Defining Navigation Destinations
 *
 * All navigation destinations must be serializable and implement `TtcNavKey`:
 *
 * ```kotlin
 * // Simple destination with no parameters
 * @Serializable
 * data object LoginDestination: TtcNavKey
 *
 * // Destination with parameters
 * @Serializable
 * data class TeeTimeDetailDestination(val teeTimeId: String): TtcNavKey
 *
 * // Nested destination within a feature
 * @Serializable
 * data class EditTeeTimeDestination(
 *     val teeTimeId: String,
 *     val returnToList: Boolean = false
 * ): TtcNavKey
 * ```
 *
 * ## Types of Destinations
 *
 * There are two main categories of destinations:
 *
 * 1. **Top-Level Destinations**: Primary app sections that maintain their own back stacks.
 *    These are defined as enum values in [TopLevelDestination].
 *
 * 2. **Feature Destinations**: Screens within a specific feature or section. These make up
 *    the back stack for each top-level destination.
 *
 * ## Serialization Requirements
 *
 * All destination classes must be annotated with `@Serializable` to enable:
 * - Type-safe navigation with the androidx.navigation3 library
 * - Automatic state preservation across configuration changes
 * - Deep linking support (future enhancement)
 *
 * All parameters within destination classes must be serializable types (primitives, Strings,
 * or other @Serializable classes).
 *
 * ## Usage in Navigation
 *
 * Destinations are used with the Navigator to navigate between screens:
 *
 * ```kotlin
 * // Navigate to a simple destination
 * navigator.navigate(LoginDestination)
 *
 * // Navigate with parameters
 * navigator.navigate(TeeTimeDetailDestination(teeTimeId = "123"))
 *
 * // Navigate and clear back stack
 * navigator.navigate(TeeTimesListDestination, clearBackStack = true)
 * ```
 *
 * ## Future Enhancements
 *
 * This interface may be extended in the future to support:
 * - Feature toggles to conditionally show/hide destinations
 * - Analytics tracking for navigation events
 * - Permission checks before navigation
 * - Deep linking configuration
 *
 * @see Navigator
 * @see TopLevelDestination
 * @see NavDisplay
 */
interface TtcNavKey: NavKey {
//    val featureToggle: FeatureToggles? get() = null
}
