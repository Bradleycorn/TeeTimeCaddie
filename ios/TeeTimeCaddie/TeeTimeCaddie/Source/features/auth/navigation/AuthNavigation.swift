//
//  AuthNavigation.swift
//  TeeTimeCaddie
//

import SwiftUI

/// Destinations within the auth flow.
///
/// The Swift twin of `AuthNavigation.kt`. These are **not** reachable from the signed-in app:
/// `TeeTimeCaddieView` branches on `SessionState`, so the whole auth tree exists only while there is
/// no complete session and disappears the moment there is one. That is why this drives its own
/// `NavigationStack` rather than being a tab in ``Navigator``.
enum AuthDestinations: Hashable {

    /// The combined email + password screen that starts both signing in and creating an account.
    case login

    /// The profile step of creating an account, reached once the Firebase account exists.
    ///
    /// Carries the **email only — never the password**. A nav key is `Hashable`, lives in a back
    /// stack and is held in memory for as long as that stack is; a password has no business in one.
    case createAccount(email: String)
}
