//
//  LoginViewModel.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 9/18/23.
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

/// Skeleton until TTC-82 builds the credentials screen.
///
/// `signIn` is here now to prove the SDK's result type works from Swift: nothing throws, and the
/// failure arrives as a value carrying its error, switched over exhaustively with SKIE's
/// `onEnum(of:)`. TTC-82 replaces the `print`s with real UI state.
@Observable
class LoginViewModel {
    private let sessionManager: SessionManager

    private(set) var isSubmitting = false

    init(sessionManager: SessionManager = AuthModule.shared.sessionManager()) {
        self.sessionManager = sessionManager
    }

    func signIn(email: String, password: String) async {
        isSubmitting = true
        defer { isSubmitting = false }

        // `try?` here absorbs cancellation ONLY. Kotlin suspend functions are always exposed to
        // Swift as `async throws`, because their completion handler must be able to deliver a
        // CancellationException — that is true regardless of return type. What changed is what can
        // arrive there: business failures are values in the result below, never throws.
        guard let result = try? await sessionManager.signIn(email: email, password: password) else {
            return
        }

        switch onEnum(of: result) {
        case .success(let success):
            // `success.data` is non-optional here: TtcResult's `T : Any` bound is what stops
            // Kotlin/Native exporting the payload as `_Nullable`.
            switch onEnum(of: success.data) {
            case .signedIn(let signedIn):
                print("Signed in as \(signedIn.player.firstName)")
            case .profileIncomplete:
                print("Profile step still to do")
            case .loading, .signedOut:
                break
            }
        case .failure(let failure):
            // Generic surface: every SDK error carries these, no cast required.
            print("Sign in failed: \(failure.error.displayMessage)")

            // Specific cases render differently, and that needs the feature's own enum.
            if let authError = failure.error as? AuthException {
                print("  reason: \(authError.error.name)")
            }
        }
    }
}
