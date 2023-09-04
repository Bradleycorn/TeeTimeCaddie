//
//  LoginScreen.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 8/25/23.
//

import Foundation
import SwiftUI

struct LoginScreen: View {
    
    private let onRegisterClick: ()->Void
    
    init(onRegisterClick: @escaping ()->Void = {}) {
        self.onRegisterClick = onRegisterClick
    }
    
    var body: some View {
        VStack {
            Text("Login")
            Button("Create Account", action: { onRegisterClick() })
        }
    }
}
