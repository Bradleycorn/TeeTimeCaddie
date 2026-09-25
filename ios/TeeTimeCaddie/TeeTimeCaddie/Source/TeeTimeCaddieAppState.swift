//
//  TeeTimeCaddieAppState.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 7/16/23.
//

import Foundation
import TeeTimeCaddieKit


enum AppUiState {
    case REGISTRATION
    case LOGIN
    case APP
    
    // Transitional: TTC-81 replaces this enum with a branch on SessionState, which is what
    // distinguishes "signed out" from "signed in but no profile yet". Until then the shell only
    // needs to know whether to show auth, and there is one credentials screen rather than two.
    static func from(_ state: SessionState) -> AppUiState {
        // Exhaustive on purpose (no `default`), so when TTC-81 replaces this enum with a real
        // three-way branch the compiler points at every case that needs rethinking. Note
        // ProfileIncomplete currently falls back to the auth screen: correct for today's stubs,
        // but it is the case that should resume the profile step.
        switch onEnum(of: state) {
        case .signedIn:
            return .APP
        case .loading, .signedOut, .profileIncomplete:
            return .LOGIN
        }
    }
}

@MainActor
@Observable
class TeeTimeCaddieAppState {
    private(set) var uiState: AppUiState
    
    private let sessionManager: SessionManager
    
    init(sessionManager: SessionManager = AuthModule.shared.sessionManager()) {
        self.sessionManager = sessionManager
        self.uiState = AppUiState.from(sessionManager.initialSessionState)
    }
    
    func observeAuthState() async {
        for await state in sessionManager.sessionState {
            uiState = AppUiState.from(state)
        }
    }
    
    func setUiState(to newState: AppUiState) {
        if (uiState != newState) { uiState = newState }

    }
}
