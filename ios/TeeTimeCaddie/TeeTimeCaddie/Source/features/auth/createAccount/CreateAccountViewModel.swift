//
//  CreateAccountViewModel.swift
//  TeeTimeCaddie
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

/// Placeholder until TTC-82 builds the profile step.
@Observable
class CreateAccountViewModel {
    private let sessionManager: SessionManager

    init(sessionManager: SessionManager = AuthModule.shared.sessionManager()) {
        self.sessionManager = sessionManager
    }
}
