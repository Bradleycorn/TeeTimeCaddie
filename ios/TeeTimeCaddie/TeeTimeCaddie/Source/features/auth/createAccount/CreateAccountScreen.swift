import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// Placeholder profile step of account creation.
///
/// TTC-82 replaces the body with the name / mobile / photo form. Until then it only has to prove
/// the shell routes correctly.
struct CreateAccountScreen: View {
    private let email: String
    private let onBack: () -> Void

    @State private var viewModel = CreateAccountViewModel()

    init(email: String, onBack: @escaping () -> Void = {}) {
        self.email = email
        self.onBack = onBack
    }

    var body: some View {
        Screen(.CreateAccount(viewName: self.viewName)) {
            CreateAccountContent(email: email)
        }
        // Every way out of this screen deletes the half-made account: the back button, the swipe
        // gesture, and `onBack` when a future "Sign in instead" pops programmatically. Centralised
        // here so no exit path can forget.
        .backNavigationHandler { viewModel.abandonSignUp() }
    }
}

fileprivate struct CreateAccountContent: View {
    let email: String

    var body: some View {
        VStack(spacing: 8) {
            Text("Profile Setup Placeholder")
            Text(email)
        }
    }
}

#Preview {
    TeeTimeCaddieTheme {
        CreateAccountContent(email: "dana@example.com")
    }
}
