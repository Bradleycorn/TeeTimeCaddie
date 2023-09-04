//
//  RegistrationScreen.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 7/18/23.
//

import SwiftUI
import TeeTimeCaddieKit

struct RegistrationScreen: View {
    @StateObject
    private var viewModel = RegistrationViewModel(
        authRepo: AuthModule.shared.authRepository(),
        eventManager: AppModule.shared.eventManager())
    
    @State private var email: String = ""
    @State private var password: String = ""
    @State private var name: String = ""

    let title = AR.strings().reg_screen_title.desc().localized()
    let buttonTitle = AR.strings().reg_submit_button.desc().localized()
    
    var body: some View {
        VStack(alignment: .center,spacing: 0) {
            Text(title)
                .font(.title)
                .bold()

            Image("icon")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(maxWidth: .infinity, maxHeight: 164)
                .foregroundColor(.blue)
                .padding(.vertical, 32)

            VStack(spacing: 0) {
                TextField("Email Address", text: $email)
                    .padding(12)
                Divider()
                    .padding(.leading, 12)
                PasswordField("Password", text: $password)
                    .padding(12)
                Divider()
                    .padding(.leading, 12)
                TextField("Name", text: $name)
                    .padding(12)
            }
            .background(Color.white)
            .cornerRadius(12)
            
            Spacer()
                .frame(height: 20)
                        
            Button(
                action: { viewModel.registerUser(email: email, password: password, name: name, onSuccess: {print("Success")}) },
                label: {Text(buttonTitle).frame(maxWidth: .infinity)}
            )
                .buttonStyle(.borderedProminent)
                .disabled(!viewModel.submitEnabled)
                .errorAlert(error: $viewModel.registrationError)
            
            Spacer()

            HStack {
                Text("Already Have an Account?")
                Button("Sign in", action: {})
            }
        }
        .padding(.horizontal, 24)
        .background(Color(UIColor.systemGroupedBackground))
    }
}






struct RegistrationScreen_Previews: PreviewProvider {
    static var previews: some View {
        RegistrationScreen()
    }
}
