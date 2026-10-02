//
//  RegistrationViewModel.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 8/6/23.
//

import Foundation
import TeeTimeCaddieKit

/// Placeholder until TTC-82 builds the create-account screen.
///
/// Its previous `registerUser(email:password:name:)` called an `AuthRepository` method that no
/// longer exists — sign-up is now two-phase and runs through `SessionManager`. Nothing called it
/// (the screens are still stubs), so it is removed here rather than half-ported.
@Observable
class RegistrationViewModel {

    private let sessionManager: SessionManager

    init(sessionManager: SessionManager = AuthModule.shared.sessionManager()) {
        self.sessionManager = sessionManager
    }
}
