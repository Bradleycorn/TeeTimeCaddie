//
//  LoginScreen.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 8/25/23.
//

import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

/// The credentials screen: one email and one password, serving both signing in and starting an
/// account.
///
/// Which of the two happens is decided by the button that is tapped. There is no mode to switch and
/// no second form — the profile step only appears once the account exists.
///
/// **No header.** An iOS divergence from Android's empty app-bar strip: the brand lockup is the
/// screen's title, and a bar above it would be a second, emptier one.
struct LoginScreen: View {
    @State private var viewModel = LoginViewModel()

    private let onCreateAccount: (String) -> Void

    init(onCreateAccount: @escaping (String) -> Void = { _ in }) {
        self.onCreateAccount = onCreateAccount
    }

    var body: some View {
        Screen(AnalyticsScreen.Login(viewName: self.viewName)) {
            LoginContent(
                state: viewModel.uiState,
                onEmailChange: viewModel.onEmailChange,
                onPasswordChange: viewModel.onPasswordChange,
                onSignIn: { Task { await viewModel.signIn() } },
                onCreateAccount: {
                    Task { await viewModel.createAccount(onCreated: onCreateAccount) }
                },
                onMessageAction: viewModel.onMessageAction
            )
        }
    }
}

// MARK: - Content

fileprivate struct LoginContent: View {
    @EnvironmentObject private var theme: AppTheme
    @FocusState private var emailFocused: Bool
    @FocusState private var passwordFocused: Bool

    let state: LoginUiState
    let onEmailChange: (String) -> Void
    let onPasswordChange: (String) -> Void
    let onSignIn: () -> Void
    let onCreateAccount: () -> Void
    let onMessageAction: () -> Void

    var body: some View {
        GeometryReader { proxy in
            ScrollView {
                VStack(spacing: Metrics.spacing) {
                    Spacer(minLength: 0)

                    BrandLockup()

                    // Between the lockup and the fields: close enough to the fields to read as
                    // being about them, far enough not to be mistaken for part of the branding.
                    if let message = state.message {
                        AuthMessageCard(message: message, onAction: onMessageAction)
                    }

                    fields
                    actions

                    TermsFooter()

                    Spacer(minLength: 0)
                }
                .padding(.horizontal, Metrics.horizontalPadding)
                .padding(.vertical, Metrics.verticalPadding)
                // Centres the content when there is room, and lets it scroll when there isn't.
                .frame(minHeight: proxy.size.height)
            }
            .scrollDismissesKeyboard(.interactively)
        }
        // The address is what someone types first, so the screen arrives ready for it.
        //
        // Deliberately delayed: a first-responder assignment made during a NavigationStack push
        // transition is dropped on the floor, so `.onAppear` is unreliable here.
        .task {
            try? await Task.sleep(for: .milliseconds(50))
            emailFocused = true
        }
    }

    private var fields: some View {
        VStack(spacing: Metrics.fieldSpacing) {
            TtcTextField(
                AR.strings().field_label_email.localized(),
                // Two-way for SwiftUI, one-way through the ViewModel: every edit routes through
                // `onEmailChange`, which is what makes "editing a field dismisses the message
                // block" a property of the architecture rather than an extra onChange handler.
                text: Binding(get: { state.email }, set: onEmailChange),
                leadingIcon: .symbol(.envelopeFill),
                keyboardType: .emailAddress,
                textContentType: .emailAddress,
                focused: $emailFocused
            )
            .disabled(state.isSubmitting)
            .submitLabel(.next)
            .onSubmit { passwordFocused = true }

            TtcPasswordField(
                text: Binding(get: { state.password }, set: onPasswordChange),
                focused: $passwordFocused
            )
            .disabled(state.isSubmitting)
            .submitLabel(.go)
            .onSubmit(onSignIn)
        }
    }

    private var actions: some View {
        VStack(spacing: Metrics.fieldSpacing) {
            TtcButton(
                AR.strings().auth_sign_in_button.localized(),
                color: .primary,
                isLoading: state.isSubmitting,
                action: onSignIn
            )
            .frame(maxWidth: .infinity)
            .disabled(!state.canSubmit)

            TtcOutlinedButton(
                AR.strings().auth_create_account_button.localized(),
                color: .primary,
                action: onCreateAccount
            )
            .frame(maxWidth: .infinity)
            .disabled(!state.canSubmit)
        }
    }

    private enum Metrics {
        static let spacing: CGFloat = 16
        static let fieldSpacing: CGFloat = 12
        static let horizontalPadding: CGFloat = 24
        static let verticalPadding: CGFloat = 24
    }
}

// MARK: - Brand lockup

/// The brand mark, name and tagline that head the screen.
///
/// The circle is `primaryContainer` rather than solid `primary`: it is an identity mark, not the
/// screen's call to action, and a solid green disc would out-shout the Sign in button below it.
fileprivate struct BrandLockup: View {
    @EnvironmentObject private var theme: AppTheme

    var body: some View {
        VStack(spacing: 8) {
            Icon(.asset(.icon), size: 36)
                .foregroundStyle(theme.colorScheme.onPrimaryContainer)
                .frame(width: 64, height: 64)
                .background(theme.colorScheme.primaryContainer, in: Circle())

            Text(AR.strings().auth_brand_name.localized())
                .font(.title.weight(.semibold))

            Text(AR.strings().auth_tagline.localized())
                .font(.subheadline)
                .foregroundStyle(theme.colorScheme.onSurfaceVariant)
                .multilineTextAlignment(.center)
        }
    }
}

// MARK: - Legal footer

/// The "By continuing you agree to…" line at the foot of the screen.
///
/// Terms and Privacy are accented but **inert** — not links, not tappable. That is deliberate and
/// not an omission: neither page exists yet, and a link that goes nowhere is worse than text that
/// never promised to.
///
/// Built by concatenating `Text` rather than with markdown, because a markdown link would take the
/// system tint *and* a tap target, which is exactly what must not happen here.
fileprivate struct TermsFooter: View {
    @EnvironmentObject private var theme: AppTheme

    var body: some View {
        let terms = AR.strings().auth_legal_terms.localized()
        let privacy = AR.strings().auth_legal_privacy.localized()
        let template = AR.strings().auth_legal_footer.localized("%@", "%@")
        let parts = template.components(separatedBy: "%@")

        return (
            Text(verbatim: parts.first ?? "")
                + accented(terms)
                + Text(verbatim: parts.count > 1 ? parts[1] : " and ")
                + accented(privacy)
                + Text(verbatim: parts.count > 2 ? parts[2] : ".")
        )
        .font(.footnote)
        .foregroundStyle(theme.colorScheme.onSurfaceVariant)
        .multilineTextAlignment(.center)
    }

    private func accented(_ word: String) -> Text {
        Text(verbatim: word).foregroundColor(theme.colorScheme.tertiary)
    }
}

// MARK: - Previews

#Preview("Light") {
    TeeTimeCaddieTheme {
        LoginContentPreviews()
    }
}

#Preview("Dark") {
    TeeTimeCaddieTheme {
        LoginContentPreviews()
    }
    .preferredColorScheme(.dark)
}

/// Never constructs ``LoginScreen``: that wrapper builds a ViewModel, which resolves the SDK and
/// kills the preview process. ``LoginContent`` takes a plain state struct, so this is pure
/// construction.
fileprivate struct LoginContentPreviews: View {
    let state: LoginUiState

    init(state: LoginUiState = LoginUiState()) {
        self.state = state
    }

    var body: some View {
        LoginContent(
            state: state,
            onEmailChange: { _ in },
            onPasswordChange: { _ in },
            onSignIn: {},
            onCreateAccount: {},
            onMessageAction: {}
        )
    }
}

#Preview("Sign-in failed") {
    TeeTimeCaddieTheme {
        LoginContentPreviews(
            state: LoginUiState(
                email: "dana@example.com",
                password: "hunter2",
                message: .signInFailed
            )
        )
    }
}

#Preview("Email in use") {
    TeeTimeCaddieTheme {
        LoginContentPreviews(
            state: LoginUiState(
                email: "dana@example.com",
                password: "hunter2",
                message: .emailInUse(email: "dana@example.com")
            )
        )
    }
}
