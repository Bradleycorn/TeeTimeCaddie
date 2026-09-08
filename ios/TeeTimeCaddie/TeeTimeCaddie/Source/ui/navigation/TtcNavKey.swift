//
//  CdiNavKey.swift
//  Native-iOS
//
//  Created by Bradley Ball on 8/29/25.
//
import SwiftUI
import TeeTimeCaddieKit

/// A protocol that defines the requirements for all navigation destinations in the app.
///
/// `TtcNavKey` provides a unified interface for navigation destinations, enabling compile-time
/// safety, authentication enforcement, and feature toggle integration. All destinations in the
/// navigation system must conform to this protocol.
///
/// ## Core Requirements
/// - **View Building**: Each destination must know how to build its associated view
/// - **Authentication**: Destinations can declare if they require user authentication
/// - **Feature Toggles**: Destinations can be controlled by feature toggles for A/B testing
/// - **Identity**: Destinations must be uniquely identifiable and comparable
///
/// ## Protocol Conformance
/// The protocol extends `Hashable`, `Equatable`, and `Identifiable` to enable:
/// - Storage in navigation stacks (requires `Hashable`)
/// - Comparison for duplicate navigation prevention (requires `Equatable`)
/// - Unique identification for SwiftUI list operations (requires `Identifiable`)
///
///
/// ## Usage Examples
/// ```swift
/// // Simple destination without parameters
/// struct SettingsDestination: TtcNavKey {
///     static let shared = SettingsDestination()
///
///     func destinationView(_ navigator: Navigator) -> some View {
///         SettingsScreen(onBackClicked: { navigator.pop() })
///     }
/// }
///
/// // Feature-based destinations with parameters
/// enum ProfileDestinations: TtcNavKey {
///     case profile
///     case editProfile
///     case changePassword(userId: String)
///
///     var authRequired: Bool { true }
///
///     func destinationView(_ navigator: Navigator) -> some View {
///         // Return appropriate view based on case
///     }
/// }
/// ```
protocol TtcNavKey: Hashable, Equatable, Identifiable {
    
    /// The feature toggle that controls access to this destination.
    ///
    /// When set, the navigation system checks if the feature is enabled before
    /// allowing navigation. If the feature is disabled, navigation attempts are
    /// silently blocked.
    ///
    /// **Default**: `nil` - Most destinations are not controlled by feature toggles
    //var featureToggle: FeatureToggles? { get }
    
    /// The type of view that this destination produces.
    ///
    /// This associated type enables compile-time type safety while allowing
    /// different destinations to return different view types.
    associatedtype Screen: View

    /// Builds and returns the SwiftUI view for this destination.
    ///
    /// This method is called by the navigation system when the destination needs
    /// to be displayed. The view should be configured with appropriate navigation
    /// callbacks to enable user interactions.
    ///
    /// **Important**: Views should receive navigation callbacks rather than direct
    /// access to the Navigator instance to maintain separation of concerns. Do not pass
    /// the navigator into the view, or make it available as an environment object.
    ///
    /// - Parameter navigator: The Navigator instance for creating navigation callbacks
    /// - Returns: The configured SwiftUI view for this destination
    ///
    /// ## Example Implementation
    /// ```swift
    /// func destinationView(_ navigator: Navigator) -> some View {
    ///     ProfileScreen(
    ///         onEditClicked: { navigator.navigateToEditProfile() },
    ///         onBackClicked: { navigator.pop() }
    ///     )
    /// }
    /// ```
    @MainActor
    @ViewBuilder
    func destinationView(_ navigator: Navigator) -> Screen
}

/// Default implementations for optional CdiNavKey properties.
///
/// These extensions provide sensible defaults for most navigation destinations,
/// allowing conforming types to only override the properties they need to customize.
extension TtcNavKey {
    /// Default implementation: destinations are not controlled by feature toggles.
    ///
    /// Override this property to return a specific `FeatureToggles` value for
    /// destinations that should be controlled by feature flags.
    //var featureToggle: FeatureToggles? { nil }
}

/// Default implementations for protocol requirements.
///
/// These implementations provide standard behavior for identity and equality
/// based on the destination's hash value, which works correctly for most cases.
extension TtcNavKey {
    /// Default implementation of Identifiable.id using the hash value.
    ///
    /// This provides a unique identifier for each destination instance that
    /// SwiftUI can use for list operations and view identity.
    var id: Int {
        self.hashValue
    }

    /// Default implementation of Equatable using id comparison.
    ///
    /// Two destinations are considered equal if they have the same id,
    /// which enables duplicate navigation prevention and proper navigation
    /// stack management.
    static func == (lhs: Self, rhs: Self) -> Bool {
        lhs.id == rhs.id
    }
}

/// Type-erased wrapper for TtcNavKey that enables storage in heterogeneous collections.
///
/// Swift's type system prevents storing different types conforming to the same protocol
/// in the same collection when the protocol has associated types. `AnyTtcNavKey` solves
/// this limitation by wrapping any `TtcNavKey` conforming type in a concrete struct.
///
/// ## Why This Is Necessary
/// Without type erasure, navigation stacks couldn't store different destination types
/// together because Swift requires collections to have a single, known element type.
/// This wrapper maintains type safety while allowing flexible navigation stack storage.
///
/// ## Usage
/// The navigation system uses this wrapper internally. Extension methods on `Navigator`
/// provide both `any TtcNavKey` and `AnyTtcNavKey` overloads for developer convenience:
///
/// ```swift
/// // Both calls are equivalent - the first automatically wraps the destination
/// navigator.navigate(to: ProfileDestinations.profile)
/// navigator.navigate(to: AnyCdiNavKey(ProfileDestinations.profile))
/// ```
///
/// ## Hash and Equality
/// The wrapper delegates hash and equality operations to the wrapped destination,
/// ensuring that wrapped destinations behave identically to their unwrapped counterparts.
struct AnyTtcNavKey: Hashable, Equatable, Identifiable {
    /// The wrapped navigation destination.
    ///
    /// This property provides access to the underlying destination while maintaining
    /// type erasure. The navigation system uses this to call destination methods
    /// and access destination properties.
    let wrapped: any TtcNavKey
    
    /// Unique identifier delegated from the wrapped destination.
    ///
    /// This ensures that the wrapper has the same identity as the wrapped destination.
    var id: Int { wrapped.id }
    
    /// Creates a type-erased wrapper around the specified navigation destination.
    ///
    /// - Parameter navKey: The destination to wrap
    init(_ navKey: any TtcNavKey) {
        self.wrapped = navKey
    }
    
    /// Hash implementation that delegates to the wrapped destination.
    ///
    /// This ensures that wrapped and unwrapped destinations have the same hash value,
    /// maintaining proper collection behavior.
    func hash(into hasher: inout Hasher) {
        wrapped.hash(into: &hasher)
    }
    
    /// Equality implementation that compares wrapped destination identities.
    ///
    /// Two `AnyTtcNavKey` instances are equal if their wrapped destinations
    /// have the same identity, regardless of the wrapper.
    static func == (lhs: AnyTtcNavKey, rhs: AnyTtcNavKey) -> Bool {
        lhs.wrapped.id == rhs.wrapped.id
    }
}
