//
//  LoginScreen.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 8/25/23.
//

import Foundation
import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

struct LoginScreen: View {
    init(onRegisterClick: @escaping ()->Void = {}) {
        self.onRegisterClick = onRegisterClick
    }

    @State
    private var viewModel = LoginViewModel(authRepo: AuthModule.shared.authRepository())

    private let onRegisterClick: ()->Void

    var body: some View {
        Screen(AnalyticsScreen.Login(viewName: self.viewName)) {
            LoginContent()
        }
    }
}

fileprivate struct LoginContent: View {
    private let onRegisterClick: ()->Void

    init(
        onRegisterClick: @escaping ()->Void = {}
    ) {
        self.onRegisterClick = onRegisterClick
    }


    var body: some View {
        Text("Login Placeholder")
    }
}

struct LoginPreview: PreviewProvider {
    static var previews: some View {
        TeeTimeCaddieTheme {
            LoginContent()
        }
    }
}


