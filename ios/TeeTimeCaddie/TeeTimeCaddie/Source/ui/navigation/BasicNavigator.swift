//
//  BasicNavigator.swift
//  TeeTimeCaddie
//

import SwiftUI

/// A ``Navigator`` with a single back stack — a flow with no tab bar.
///
/// The auth flow uses this. It is the whole of ``Navigator`` and nothing more, so a feature written
/// against the protocol runs here or under ``TabNavigator`` unchanged. The Android twin is
/// `BasicNavigator`.
///
/// Seeding more than one destination gives a flow somewhere to go back *to* when it is entered part
/// way through — which is what a restored half-finished sign-up does: the stack starts at the
/// credentials screen with the profile step above it, so abandoning has a destination rather than a
/// dead end.
@MainActor
@Observable
final class BasicNavigator: Navigator {

    /// The stack, for a `NavigationStack` to render.
    ///
    /// Unlike ``TabNavigator`` there is only one, so this is unambiguous.
    var backstack: [AnyTtcNavKey]

    init(_ startDestinations: [any TtcNavKey] = []) {
        self.backstack = startDestinations.map(AnyTtcNavKey.init)
    }

    var currentDestination: AnyTtcNavKey? { backstack.last }

    func navigate(to navKey: AnyTtcNavKey, clearBackStack: Bool) {
        // Already there, and not being asked to rebuild the stack.
        if !clearBackStack && backstack.last == navKey { return }

        if clearBackStack { clearbackstack() }
        backstack.append(navKey)
    }

    func pop() {
        if !backstack.isEmpty { backstack.removeLast() }
    }

    func popUpTo(to navKey: AnyTtcNavKey, inclusive: Bool) {
        guard let index = backstack.firstIndex(of: navKey) else { return }
        backstack.popUpTo(index: index, inclusive: inclusive)
    }

    func clearbackstack() {
        backstack.removeAll()
    }
}
