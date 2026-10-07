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
    /// Seeded from the SDK flow's synchronous `value`, which is already the right first frame: a cold
    /// start with no persisted session goes straight to the credentials screen instead of flashing a
    /// loading state first.
    private(set) var sessionState: SessionState

    private let sessionManager: SessionManager
    private let toastPresenter: TtcToastPresenter

    init(
        sessionManager: SessionManager = AuthModule.shared.sessionManager(),
        toastPresenter: TtcToastPresenter = AppModule.shared.toastPresenter()
    ) {
        self.sessionManager = sessionManager
        self.toastPresenter = toastPresenter
        self.sessionState = sessionManager.sessionState.value
    }

    func observeSessionState() async {
        for await state in sessionManager.sessionState {
            sessionState = state
        }
    }

    /// Confirms each session transition the person caused — "Welcome back, Dana", "Signed out".
    ///
    /// Here, rather than in the screen that caused it, because that screen is destroyed by the very
    /// transition it would be confirming: the root swaps the whole tree when the session changes. The
    /// Android twin is `TeeTimeCaddieActivityViewModel`.
    func observeSessionEvents() async {
        for await event in sessionManager.sessionEvents {
            switch onEnum(of: event) {
            case .signedIn(let signedIn):
                toastPresenter.show(AR.strings().auth_toast_welcome_back.localized(signedIn.player.firstName))
            case .accountCreated(let created):
                toastPresenter.show(AR.strings().auth_toast_account_created.localized(created.player.firstName))
            case .signedOut:
                toastPresenter.show(AR.strings().auth_toast_signed_out.localized())
            }
        }
    }

    /// Re-validate the session, e.g. when the app returns to the foreground.
    func refreshSession() async {
        try? await sessionManager.refreshSession()
    }
}
