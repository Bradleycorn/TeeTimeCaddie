//
//  ProfileViewModel.swift
//  TeeTimeCaddie
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

/// State of the Profile tab.
///
/// There is no error case: the player shown here is the one already held in
/// `SessionState.SignedIn`, so reaching this screen at all means the read succeeded.
///
/// Free of SDK types so ``ProfileContent`` previews are pure construction — `Player` is a KMP class,
/// and touching it from a preview would pull in the framework.
struct ProfileUiState: Equatable {
    var name: String = ""
    var email: String = ""

    /// Already formatted for display; the SDK holds digits.
    var phone: String = ""

    var photoUrl: String?

    /// The avatar's fallback when there is no photo, or it hasn't loaded.
    var initial: String = ""

    var isLoaded: Bool = false
}

/// The Swift twin of `ProfileViewModel.kt` — same state, same method names.
@MainActor
@Observable
final class ProfileViewModel {

    private(set) var uiState = ProfileUiState()

    private let sessionManager: SessionManager

    init(sessionManager: SessionManager = AuthModule.shared.sessionManager()) {
        self.sessionManager = sessionManager
        apply(sessionManager.sessionState.value)
    }

    func observeSession() async {
        for await state in sessionManager.sessionState {
            apply(state)
        }
    }

    /// Signs the player out.
    ///
    /// Nothing navigates afterwards — the root swaps the whole tree when `SessionState` becomes
    /// `SignedOut` — and nothing is confirmed from here: the root shows "Signed out" on
    /// `SessionEvent.SignedOut`, because this ViewModel is destroyed by that swap.
    ///
    /// Not `async`: `SessionManager.signOut` owns the scope its work runs in, precisely because
    /// every caller is a screen that signing out destroys.
    func signOut() {
        sessionManager.signOut()
    }

    private func apply(_ state: SessionState) {
        guard let signedIn = state as? SessionState.SignedIn else { return }
        let player = signedIn.player
        uiState = ProfileUiState(
            name: player.name,
            email: player.email,
            phone: player.formattedPhone,
            photoUrl: player.photoUrl,
            initial: player.initial,
            isLoaded: true
        )
    }
}
