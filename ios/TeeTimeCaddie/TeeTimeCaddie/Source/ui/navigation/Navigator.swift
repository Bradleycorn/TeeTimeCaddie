//
//  Navigator.swift
//  TeeTimeCaddie
//

import SwiftUI

/// # Navigation System
///
/// A `Navigator` owns the back stack that a `NavigationStack` renders, and is the single vocabulary
/// every feature's navigation is written against.
///
/// ## Implementations
///
/// The app has two navigation contexts, and one implementation for each:
///
/// - ``TabNavigator`` — a stack **per** ``AppTabs``, for the tabbed, signed-in app. Switching tabs
///   preserves where you were in each one. It backs ``TabsNavView``.
/// - ``BasicNavigator`` — a single stack, for a flow with no tab bar. It backs ``AuthNavView``.
///
/// Everything a feature needs is on this protocol, so **feature navigation extensions are declared
/// on `Navigator`, not on an implementation**:
///
/// ```swift
/// extension Navigator {
///     func navigateToAddTeeTime() {
///         navigate(to: TeeTimesDestinations.addTeeTime)
///     }
/// }
/// ```
///
/// Written that way, a feature can be hosted in either context without being rewritten, and there
/// is only one navigation architecture to learn. The Android twin is the `Navigator` interface with
/// `NavBarNavigator` and `BasicNavigator`.
///
/// ## Why the back stack is not on this protocol
///
/// Each implementation exposes its own `Binding` for a `NavigationStack` to render, but the two are
/// not the same question. ``BasicNavigator`` has one stack; ``TabNavigator`` has one *per tab*, and
/// ``TabNavStack`` renders a **named** tab's stack rather than "the current one". Putting a single
/// `backstack` here would invite rendering the wrong one. The views that own a `NavigationStack`
/// therefore hold a concrete navigator; features only ever need the operations below.
///
/// ## Navigation Best Practices
///
/// **DO NOT** pass a Navigator into screens or view models. Screens accept lambda callbacks, wired
/// up in the `destinationView(_:)` implementations. See ``TtcNavKey``.
@MainActor
protocol Navigator: AnyObject, Observable {

    /// The destination currently on screen, or nil at the root of the stack.
    var currentDestination: AnyTtcNavKey? { get }

    /// Navigates to `navKey`.
    ///
    /// - Parameters:
    ///   - navKey: The destination to show. ``TabNavigator`` additionally understands an
    ///     ``AppTabs`` here, and switches tabs rather than pushing.
    ///   - clearBackStack: Clears the current stack first, so `navKey` becomes the only thing left
    ///     to go back to.
    func navigate(to navKey: AnyTtcNavKey, clearBackStack: Bool)

    /// Removes the topmost destination. Does nothing at the root of the stack.
    func pop()

    /// Pops until `navKey` is on top, optionally removing it too.
    func popUpTo(to navKey: AnyTtcNavKey, inclusive: Bool)

    /// Removes every destination, returning to the stack's root.
    func clearbackstack()
}

// MARK: - Convenience overloads

/// Wrapping overloads, so call sites never spell `AnyTtcNavKey` themselves.
///
/// On the protocol rather than each implementation: they are pure forwarding, and an implementation
/// that redeclared one could silently diverge.
extension Navigator {

    func navigate(to navKey: AnyTtcNavKey) {
        navigate(to: navKey, clearBackStack: false)
    }

    func navigate(to key: any TtcNavKey, clearBackStack: Bool = false) {
        navigate(to: AnyTtcNavKey(key), clearBackStack: clearBackStack)
    }

    func popUpTo(to navKey: AnyTtcNavKey) {
        popUpTo(to: navKey, inclusive: false)
    }

    func popUpTo(to key: any TtcNavKey, inclusive: Bool = false) {
        popUpTo(to: AnyTtcNavKey(key), inclusive: inclusive)
    }
}

// MARK: - Back stack helpers

/// Utility methods for array-based navigation stack manipulation.
extension [AnyTtcNavKey] {
    /// Removes elements up to and optionally including the specified index.
    ///
    /// - Parameters:
    ///   - index: The index to pop up to
    ///   - inclusive: If `true`, removes the element at the index; if `false`, keeps it
    mutating func popUpTo(index: Int, inclusive: Bool = false) {
        if inclusive {
            removeSubrange(index...)
        } else if index + 1 < count {
            removeSubrange((index+1)...)
        }
    }
}
