//
//  LoginViewModel.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 9/18/23.
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

/// Placeholder until TTC-82 builds the credentials screen.
@Observable
class LoginViewModel {
    private let sessionManager: SessionManager

    init(sessionManager: SessionManager = AuthModule.shared.sessionManager()) {
        self.sessionManager = sessionManager
    }
}
