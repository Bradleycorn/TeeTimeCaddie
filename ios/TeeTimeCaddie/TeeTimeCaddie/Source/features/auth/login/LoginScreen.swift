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

/// Placeholder credentials screen.
///
/// TTC-82 replaces the body with the brand lockup, fields, message block and legal footer.
struct LoginScreen: View {
    @State private var viewModel = LoginViewModel()

    private let onCreateAccount: (String) -> Void

    init(onCreateAccount: @escaping (String) -> Void = { _ in }) {
        self.onCreateAccount = onCreateAccount
    }

    var body: some View {
        Screen(AnalyticsScreen.Login(viewName: self.viewName)) {
            LoginContent(onCreateAccount: { onCreateAccount(placeholderEmail) })
        }
    }

    private var placeholderEmail: String { "placeholder@example.com" }
}

fileprivate struct LoginContent: View {
    let onCreateAccount: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Text("Credentials Screen Placeholder")
            TtcOutlinedButton(
                AR.strings().auth_create_account_button.desc().localized(),
                action: onCreateAccount
            )
        }
    }
}

#Preview {
    TeeTimeCaddieTheme {
        LoginContent(onCreateAccount: {})
    }
}


