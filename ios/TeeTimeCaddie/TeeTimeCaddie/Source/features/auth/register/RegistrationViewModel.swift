//
//  RegistrationViewModel.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 8/6/23.
//

import Foundation
import TeeTimeCaddieKit
import KMPNativeCoroutinesAsync

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
    private(set) var submitEnabled = true
    
    @Published
    var registrationError: TeeTimeCaddieError? = nil
    
    func registerUser(email: String, password: String, name: String, onSuccess: @escaping ()->Void) {
        Task {
            submitEnabled = false
            defer { submitEnabled = true }
            
            do {
                _ = try await asyncFunction(for: authRepo.registerUser(email: email, password: password, name: name))
                onSuccess()
            } catch {
                let ex = error.asTeeTimeCaddieError(defaultTitle: AR.strings().reg_error_default_title)
                print(ex.logMessage)
                registrationError = ex
            }
        }
    }
}
