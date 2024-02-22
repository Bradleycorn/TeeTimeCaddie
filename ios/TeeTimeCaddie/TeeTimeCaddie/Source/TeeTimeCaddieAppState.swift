//
//  TeeTimeCaddieAppState.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 7/16/23.
//

import Foundation
import TeeTimeCaddieKit


enum UiState {
    case REGISTRATION
    case LOGIN
    case APP
    
    static func fromLoginState(_ isLoggedIn: Bool, hasLoggedInOnce: Bool) -> UiState {
        print("Is logged in: \(isLoggedIn)")
        if (isLoggedIn) {
            return .APP
        } else if (hasLoggedInOnce) {
            return .LOGIN
        } else {
            return .REGISTRATION
        }
    }
}

@MainActor
class TeeTimeCaddieAppState: ObservableObject {
    @Published
    private(set) var uiState: UiState
    
    private let authRepo: AuthRepository
    
    init(authRepo: AuthRepository = AuthModule.shared.authRepository()) {
        self.authRepo = authRepo
        self.uiState = UiState.fromLoginState(authRepo.isLoggedIn, hasLoggedInOnce: authRepo.hasLoggedInOnce)
    }
    
    func observeAuthState() async {
        for await isLoggedIn in authRepo.loginState {
            uiState = UiState.fromLoginState(isLoggedIn.boolValue, hasLoggedInOnce: authRepo.hasLoggedInOnce)
        }
    }
    
    func setUiState(to newState: UiState) {
        if (uiState != newState) { uiState = newState }

    }
}
