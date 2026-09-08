import SwiftUI
import ThemeUI
import TeeTimeCaddieKit



struct RegistrationScreen: View {
    private let onLoginClick: ()->Void

    init(onLoginClick: @escaping () -> Void = {}) {
        self.onLoginClick = onLoginClick
    }

    @State
    private var viewModel = RegistrationViewModel(authRepo: AuthModule.shared.authRepository())

    var body: some View {
        Screen(.Registration(viewName: self.viewName)) {
            RegistrationContent(onLoginClick: onLoginClick)
        }
    }
}

fileprivate struct RegistrationContent: View {

    private let onLoginClick: ()->Void

    init(onLoginClick: @escaping ()->Void = {}) {
        self.onLoginClick = onLoginClick
    }

    var body: some View {
        Text("Registration Placeholder")
    }
}


struct RegistrationScreen_Previews: PreviewProvider {
    static var previews: some View {
        TeeTimeCaddieTheme {
            RegistrationContent()
        }
    }
}
