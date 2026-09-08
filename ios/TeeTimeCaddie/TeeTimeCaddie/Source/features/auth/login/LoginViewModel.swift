//
//  LoginViewModel.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 9/18/23.
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

@Observable
class LoginViewModel {
    private let authRepo: AuthRepository
    
    init(authRepo: AuthRepository) {
        self.authRepo = authRepo
    }
   
}
