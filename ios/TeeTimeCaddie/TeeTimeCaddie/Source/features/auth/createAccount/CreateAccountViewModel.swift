//
//  CreateAccountViewModel.swift
//  TeeTimeCaddie
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

/// Placeholder until TTC-82 builds the profile step.
@MainActor
@Observable
class CreateAccountViewModel {
    private let sessionManager: SessionManager

    /// Whether sign-up finished successfully.
    ///
    /// Guards [abandonSignUp] against the one case where leaving this screen must *not* clean up:
    /// completing sign-up also removes the screen, because the whole auth tree is replaced once
    /// `SessionState` becomes `SignedIn`. `SessionManager.abandonSignUp` will not delete a
    /// provisioned account — but it does sign out unconditionally, which would bounce someone
    /// straight back out of the account they just made.
    ///
    /// TTC-82 sets this when `completeSignUp` succeeds.
    private var didCompleteSignUp = false

    init(sessionManager: SessionManager = AuthModule.shared.sessionManager()) {
        self.sessionManager = sessionManager
    }

    /// Abandons a sign-up that never got a profile, deleting the half-made account.
    ///
    /// Called from the screen's back-navigation handler, so it covers every way out: the back
    /// button, the swipe gesture, and a programmatic pop.
    ///
    /// Nothing is awaited — `SessionManager` runs this in a scope that outlives the view, precisely
    /// because the act that triggers it is what tears the view down.
    func abandonSignUp() {
        guard !didCompleteSignUp else { return }
        sessionManager.abandonSignUp(reason: "back")
    }
}
