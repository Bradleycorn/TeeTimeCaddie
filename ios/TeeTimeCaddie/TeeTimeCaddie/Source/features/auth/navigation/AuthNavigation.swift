//
//  AuthNavigation.swift
//  TeeTimeCaddie
//

import SwiftUI
import TeeTimeCaddieKit

/// Destinations within the auth flow.
///
/// The Swift twin of `AuthNavigation.kt`, and written exactly like every other feature's
/// navigation: the destinations, their views, and the functions that navigate to them, all here.
///
/// These are **not** reachable from the signed-in app. `TeeTimeCaddieView` branches on
/// `SessionState`, so the whole auth tree exists only while there is no complete session and
/// disappears the moment there is one — which is why ``AuthNavView`` drives it with a
/// ``BasicNavigator`` of its own rather than a tab's stack.
enum AuthDestinations: @MainActor TtcNavKey {

    /// The combined email + password screen that starts both signing in and creating an account.
    case login

    /// The profile step of creating an account, reached once the Firebase account exists.
    ///
    /// Carries the **email only — never the password**. A nav key is `Hashable`, lives in a back
    /// stack and is held for as long as that stack is; a password has no business in one.
    case createAccount(email: String)

    @ViewBuilder
    func destinationView(_ navigator: any Navigator) -> some View {
        switch self {
        case .login:
            LoginScreen(onCreateAccount: { navigator.navigateToCreateAccount(email: $0) })

        case .createAccount(let email):
            // No cleanup callback: the screen's own back-navigation handler owns that, so it runs
            // for the system back button and the swipe gesture too — neither of which passes
            // through here.
            CreateAccountScreen(email: email, onBack: { navigator.pop() })
                .navigationTitle(AR.strings().create_account_title.desc().localized())
                .navigationBarTitleDisplayMode(.inline)
        }
    }
}

extension Navigator {
    /// Navigates to the profile step of creating an account.
    ///
    /// Pushes rather than replaces, so going back returns to the credentials screen.
    ///
    /// Declared on ``Navigator`` rather than ``BasicNavigator`` so the auth screens could be hosted
    /// in a tab without a rewrite — the same rule every other feature follows.
    func navigateToCreateAccount(email: String) {
        navigate(to: AuthDestinations.createAccount(email: email))
    }
}
