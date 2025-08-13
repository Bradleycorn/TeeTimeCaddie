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
    
    static func fromLoginState(_ isLoggedIn: Bool, hasLoggedInOnce: Bool) -> AppUiState {
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
    private(set) var uiState: AppUiState
    
    private let authRepo: AuthRepository
    
    init(authRepo: AuthRepository = AuthModule.shared.authRepository()) {
        self.authRepo = authRepo
        self.uiState = AppUiState.fromLoginState(authRepo.isLoggedIn, hasLoggedInOnce: authRepo.hasLoggedInOnce)
    }
    
    func observeAuthState() async {
        for await isLoggedIn in authRepo.loginState {
            uiState = AppUiState.fromLoginState(isLoggedIn.boolValue, hasLoggedInOnce: authRepo.hasLoggedInOnce)
        }
    }
    
    func setUiState(to newState: AppUiState) {
        if (uiState != newState) { uiState = newState }

    }
}
