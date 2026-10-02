//
//  TeeTimeCaddieAppState.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 7/16/23.
//

import Foundation
import TeeTimeCaddieKit

/// What the root view renders, derived from the SDK's `SessionState`.
///
/// There is deliberately no app-specific UI-state enum between the two any more. `SessionState` is
/// the integration contract — **route on it, never on "is there a Firebase user"** — and a parallel
/// enum here could only ever drift from it or lose the distinction it exists to make.
@MainActor
@Observable
class TeeTimeCaddieAppState {

    /// The current session.
    ///
    /// Seeded from `initialSessionState` rather than `.Loading` so a cold start with no persisted
    /// session goes straight to the credentials screen instead of flashing a loading state first.
    private(set) var sessionState: SessionState

    private let sessionManager: SessionManager

    init(sessionManager: SessionManager = AuthModule.shared.sessionManager()) {
        self.sessionManager = sessionManager
        self.sessionState = sessionManager.initialSessionState
    }

    func observeSessionState() async {
        for await state in sessionManager.sessionState {
            sessionState = state
        }
    }

    /// Re-validate the session, e.g. when the app returns to the foreground.
    func refreshSession() async {
        try? await sessionManager.refreshSession()
    }

    /// Abandons a sign-up that never got a profile.
    ///
    /// Not `async` and nothing is awaited: `SessionManager` launches this in a scope that outlives
    /// the view, precisely because the back press that triggers it is what tears the view down.
    func abandonSignUp() {
        sessionManager.abandonSignUp(reason: "back")
    }
}
