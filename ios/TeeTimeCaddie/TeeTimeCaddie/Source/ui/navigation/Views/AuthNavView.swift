//
//  AuthNavView.swift
//  TeeTimeCaddie
//

import SwiftUI
import TeeTimeCaddieKit

/// Top-level navigation for the **signed-out** app: the auth flow's own `NavigationStack`.
///
/// The counterpart to ``TabsNavView``. `TeeTimeCaddieView` picks between the two by branching on
/// `SessionState` — they are alternatives, never destinations of one another, which is what stops
/// signing out from leaving a credentials screen stacked on a stale tee-times history.
///
/// Deliberately *not* driven by ``Navigator``: auth is not a section of the app the way Games and
/// Profile are, so it keeps its own stack rather than borrowing a tab's. The Android twin is
/// `AuthNavDisplay`.
struct AuthNavView: View {

    /// The current session. Read once, on appearance, to seed the stack.
    let sessionState: SessionState

    @State private var path: [AuthDestinations] = []

    /// The email typed on the credentials screen.
    ///
    /// Owned here rather than by ``LoginScreen``, so popping back from the profile step restores it
    /// even though the screen below was torn down. The Android twin gets this from the nav entry's
    /// own `ViewModelStore`; SwiftUI has no equivalent, so the container holds it.
    @State private var email = ""

    var body: some View {
        NavigationStack(path: $path) {
            LoginScreen(onCreateAccount: { typedEmail in
                email = typedEmail
                path.append(.createAccount(email: typedEmail))
            })
            .navigationDestination(for: AuthDestinations.self) { destination in
                switch destination {
                case .login:
                    LoginScreen(onCreateAccount: { _ in })
                case .createAccount(let email):
                    // No cleanup callback: the screen's own back-navigation handler owns that, so
                    // it runs for the system back button and the swipe gesture too — neither of
                    // which passes through here.
                    CreateAccountScreen(email: email, onBack: pop)
                        .navigationTitle(AR.strings().create_account_title.desc().localized())
                        .navigationBarTitleDisplayMode(.inline)
                }
            }
        }
        // A restored half-finished sign-up resumes at the profile step rather than at the
        // credentials screen, with the credentials screen still underneath to go back to.
        .onAppear {
            if case .profileIncomplete(let incomplete) = onEnum(of: sessionState), path.isEmpty {
                email = incomplete.email
                path = [.createAccount(email: incomplete.email)]
            }
        }
    }

    /// Pops the stack. Cleanup is the popped screen's own business.
    private func pop() {
        if !path.isEmpty { path.removeLast() }
    }
}

#Preview {
    TeeTimeCaddieTheme {
        AuthNavView(sessionState: SessionState.SignedOut())
    }
}
