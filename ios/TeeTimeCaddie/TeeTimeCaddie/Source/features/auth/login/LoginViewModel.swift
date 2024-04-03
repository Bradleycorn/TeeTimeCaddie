//
//  LoginViewModel.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 9/18/23.
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

@MainActor
class LoginViewModel: ObservableObject {
    
    init(authRepo: AuthRepository, eventManager: EventManager) {
        self.authRepo = authRepo
        self.eventManager = eventManager
    }
    
    private let authRepo: AuthRepository
    private let eventManager: EventManager

    @Published
    private(set) var processingLogin: Bool = false
    
    @Published
    var loginError: TeeTimeCaddieError? = nil
    
    func loginUser(email: String, password: String) {
        Task {
            processingLogin = true
            defer { processingLogin = false }
            
            do {
                try await authRepo.login(email: email, password: password)
            } catch {
                let ex = error.asTeeTimeCaddieError(defaultTitle: AR.strings().reg_error_default_title)
                print(ex.logMessage)
                loginError = ex
            }
        }
    }    
}
