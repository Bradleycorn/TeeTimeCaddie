//
//  ProfileViewModel.swift
//  TeeTimeCaddie
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

/// State for the Profile screen.
///
/// There is no error case: the player shown here is the one already held in
/// `SessionState.SignedIn`, so reaching this screen at all means the read succeeded.
@MainActor
@Observable
class ProfileViewModel {
    private(set) var player: Player?

    private let sessionManager: SessionManager

    init(sessionManager: SessionManager = AuthModule.shared.sessionManager()) {
        self.sessionManager = sessionManager
        self.player = (sessionManager.initialSessionState as? SessionState.SignedIn)?.player
    }

    /// Signs the player out.
    ///
    /// Nothing navigates afterwards — the root swaps the whole tree when `SessionState` becomes
    /// `SignedOut`.
    func signOut() {
        Task { try? await sessionManager.signOut() }
    }
}
