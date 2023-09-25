//
//  LoginScreen.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 8/25/23.
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

struct LoginScreen: View {
    init(onRegisterClick: @escaping ()->Void = {}) {
        self.onRegisterClick = onRegisterClick
    }

    @StateObject
    private var viewModel = LoginViewModel(
        authRepo: AuthModule.shared.authRepository(),
        eventManager: AppModule.shared.eventManager())
    
    private let onRegisterClick: ()->Void
    
    var body: some View {
        Login(
            isProcessing: viewModel.processingLogin,
            error: $viewModel.loginError,
            onLoginClick: viewModel.loginUser(email:password:),
            onRegisterClick: onRegisterClick
        )
    }
}

fileprivate struct Login: View {
    private let isProcessing: Bool
    private let error: Binding<TeeTimeCaddieError?>
    private let onLoginClick: (String, String)->Void
    private let onRegisterClick: ()->Void
    
    init(
        isProcessing: Bool,
        error: Binding<TeeTimeCaddieError?>,
        onLoginClick: @escaping (String, String)->Void = {_,_ in},
        onRegisterClick: @escaping ()->Void = {}
    ) {
        self.isProcessing = isProcessing
        self.error = error
        self.onLoginClick = onLoginClick
        self.onRegisterClick = onRegisterClick
    }
    
    @State private var email: String = ""
    @State private var password: String = ""
    
    private let screenTitle = AR.strings().login_screen_title.desc().localized()
    private let registrationPrompt = AR.strings().login_register_prompt.desc().localized()
    private let registerButtonText = AR.strings().login_register_button.desc().localized()
    private let emailLabel = AR.strings().field_label_email.desc().localized()
    private let passwordLabel = AR.strings().field_label_password.desc().localized()
    private let loginButtonLabel = AR.strings().login_submit_button.desc().localized()
    
    var body: some View {
        AuthScreen(
            analyticsScreen: AnalyticsScreen.Login.shared,
            title: screenTitle,
            footerText: registrationPrompt,
            footerActionText: registerButtonText,
            onFooterActionClick: onRegisterClick) {
                
                FormStack {
                    TextField(emailLabel, text: $email)
                        .formFieldPadding()

                    FormFieldDivider()
                    
                    PasswordField(passwordLabel, text: $password)
                        .formFieldPadding()
                }
                
                Spacer()
                    .frame(height: 20)
                
                LoadingButton(isLoading: isProcessing, action: { onLoginClick(email, password) }) {
                    Text(loginButtonLabel)
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .errorAlert(error: error)
        }
    }
}

struct LoginPreview: PreviewProvider {
    static var previews: some View {
        Login(isProcessing: false, error: .constant(nil))
    }
}


