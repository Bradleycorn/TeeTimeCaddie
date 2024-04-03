import SwiftUI
import ThemeUI
import TeeTimeCaddieKit



struct RegistrationScreen: View {
    private let onLoginClick: ()->Void
    
    init(onLoginClick: @escaping () -> Void = {}) {
        self.onLoginClick = onLoginClick
    }
    
    @StateObject
    private var viewModel = RegistrationViewModel(
        authRepo: AuthModule.shared.authRepository(),
        eventManager: AppModule.shared.eventManager())
    
    var body: some View {
        Registration(
            isProcessing: viewModel.processingRegistration,
            error: $viewModel.registrationError,
            onSubmitRegistration: viewModel.registerUser(email:password:name:),
            onLoginClick: onLoginClick)
    }
}

fileprivate struct Registration: View {

    private let isProcessing: Bool
    private let error: Binding<TeeTimeCaddieError?>
    private let onLoginClick: ()->Void
    private let onSubmitRegistration: (String,String,String)->Void
    
    init(
        isProcessing: Bool,
        error: Binding<TeeTimeCaddieError?>,
        onSubmitRegistration: @escaping (String,String,String)->Void = {_,_,_ in },
        onLoginClick: @escaping ()->Void = {}
    ) {
        self.isProcessing = isProcessing
        self.error = error
        self.onLoginClick = onLoginClick
        self.onSubmitRegistration = onSubmitRegistration
    }
        
    @State private var email: String = ""
    @State private var password: String = ""
    @State private var name: String = ""

    private let title = AR.strings().reg_screen_title.desc().localized()
    private let buttonTitle = AR.strings().reg_submit_button.desc().localized()
    private let footerText = AR.strings().reg_existing_account_prompt.desc().localized()
    private let footerAction = AR.strings().reg_login_button.desc().localized()
    private let emailLabel = AR.strings().field_label_email.desc().localized()
    private let passwordLabel = AR.strings().field_label_password.desc().localized()
    private let nameLabel = AR.strings().field_label_name.desc().localized()
    
    var body: some View {
        AuthScreen(analyticsScreen: .Registration.shared,
                   title: title,
                   footerText: footerText,
                   footerActionText: footerAction,
                   onFooterActionClick: onLoginClick) {

            FormStack {
                TextField(emailLabel, text: $email)
                    .formFieldPadding()
                
                FormFieldDivider()
                
                PasswordField(passwordLabel, text: $password)
                    .formFieldPadding()

                FormFieldDivider()
                    
                TextField(nameLabel, text: $name)
                    .formFieldPadding()
            }
            
            Spacer()
                .frame(height: 20)
                        
            LoadingButton(
                isLoading: isProcessing,
                action: { onSubmitRegistration(email, password, name)
                    
                }) {
                    Text(buttonTitle)
                        .frame(maxWidth: .infinity)
                }
            .buttonStyle(.Filled)
            .errorAlert(error: error)
        }
    }
}


struct RegistrationScreen_Previews: PreviewProvider {
    static var previews: some View {
        TeeTimeCaddieTheme {
            Registration(isProcessing: false, error: .constant(nil))
        }
    }
}
