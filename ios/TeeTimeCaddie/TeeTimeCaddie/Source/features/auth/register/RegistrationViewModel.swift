//
//  RegistrationViewModel.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 8/6/23.
//

import Foundation
import TeeTimeCaddieKit

enum AuthError: LocalizedError {
    case RegistrationError
}

@MainActor
class RegistrationViewModel: ObservableObject {
    init(authRepo: AuthRepository, eventManager: EventManager) {
        self.authRepo = authRepo
        self.eventManager = eventManager
    }
    
    private let authRepo: AuthRepository
    private let eventManager: EventManager
    
    @Published
    private(set) var processingRegistration = false
    
    @Published
    var registrationError: TeeTimeCaddieError? = nil
    
    func registerUser(email: String, password: String, name: String) {
        Task {
            processingRegistration = true
            defer { processingRegistration = false }
            
            do {
                try await authRepo.registerUser(email: email, password: password, name: name)
            } catch {
                let ex = error.asTeeTimeCaddieError(defaultTitle: AR.strings().reg_error_default_title)
                print(ex.logMessage)
                registrationError = ex
            }
        }
    }
}
