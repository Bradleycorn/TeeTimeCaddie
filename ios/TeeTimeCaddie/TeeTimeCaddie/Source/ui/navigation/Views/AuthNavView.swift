//
//  AuthNavView.swift
//  TeeTimeCaddie
//

import SwiftUI
import TeeTimeCaddieKit

/// Top-level navigation for the **signed-out** app: the auth flow's `NavigationStack`.
///
/// The counterpart to ``TabsNavView``. `TeeTimeCaddieView` picks between the two by branching on
/// `SessionState` — they are alternatives, never destinations of one another, which is what stops
/// signing out from leaving a credentials screen stacked on a stale tee-times history.
///
/// Driven by a ``BasicNavigator`` rather than a ``TabNavigator``: auth is not a section of the app
/// the way Games and Profile are, so it keeps its own stack instead of borrowing a tab's. Routing is
/// otherwise identical to ``TabNavStack``, so a feature does not care which one is hosting it. The
/// Android twin is `AuthNavDisplay`.
struct AuthNavView: View {

    /// The current session. Read once, to seed the stack.
    private let sessionState: SessionState

    /// Created here rather than passed in, so the stack lives exactly as long as this view does —
    /// signing in discards it instead of leaving a half-finished sign-up behind the tabs.
    @State private var navigator: BasicNavigator

    init(sessionState: SessionState) {
        self.sessionState = sessionState
        self.navigator = BasicNavigator(Self.initialStack(for: sessionState))
    }

    // The typed email needs no holding here: a NavigationStack's root view stays alive while
    // destinations are pushed above it, so LoginScreen's own state survives a push and pop. The
    // Android twin gets the same from the nav entry's ViewModelStore.
    var body: some View {
        NavigationStack(path: $navigator.backstack) {
            AuthDestinations.login.destinationView(navigator)
                .navigationDestination(for: AnyTtcNavKey.self) { key in
                    switch key.wrapped {
                    case let navKey as AuthDestinations:
                        navKey.destinationView(navigator)
                    default:
                        fatalError("Unhandled navigation key: \(key)")
                    }
                }
        }
    }

    /// What the stack starts with.
    ///
    /// A restored `SessionState.ProfileIncomplete` — someone who force-quit mid-sign-up — resumes at
    /// the profile step rather than the credentials screen. The credentials screen is **not** seeded
    /// beneath it: it is this stack's root already, so abandoning has somewhere to land.
    private static func initialStack(for sessionState: SessionState) -> [any TtcNavKey] {
        if case .profileIncomplete(let incomplete) = onEnum(of: sessionState) {
            return [AuthDestinations.createAccount(email: incomplete.email)]
        }
        return []
    }
}

#Preview {
    TeeTimeCaddieTheme {
        AuthNavView(sessionState: SessionState.SignedOut())
    }
}
