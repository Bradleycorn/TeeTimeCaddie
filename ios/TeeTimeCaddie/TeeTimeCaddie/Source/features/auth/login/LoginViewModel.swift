//
//  LoginViewModel.swift
//  TeeTimeCaddie
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

/// State of the credentials screen.
///
/// One screen serves both signing in and starting an account, so there is no "mode" here — which of
/// the two happens is decided by the button that was tapped, not by state.
///
/// There is no `emailError`/`passwordError`: after the design update this screen has no inline field
/// errors. Everything it has to say, it says in ``message``.
///
/// `Equatable` and free of SDK types, so ``LoginContent`` previews are pure construction.
struct LoginUiState: Equatable {
    var email: String = ""
    var password: String = ""
    var message: AuthMessage?
    var isSubmitting: Bool = false

    /// Whether either action can be taken.
    ///
    /// The same for both buttons: both need an address that could be real and a password that was
    /// typed. Password *rules* are Firebase's to enforce on sign-up, and applying them here would
    /// lock out an existing account whose password predates them.
    var canSubmit: Bool {
        email.isValidEmail && !password.isEmpty && !isSubmitting
    }
}

/// The Swift twin of `LoginViewModel.kt` — same state, same method names.
@MainActor
@Observable
final class LoginViewModel {

    private(set) var uiState = LoginUiState()

    private let sessionManager: SessionManager
    private let toastPresenter: TtcToastPresenter

    init(
        sessionManager: SessionManager = AuthModule.shared.sessionManager(),
        toastPresenter: TtcToastPresenter = AppModule.shared.toastPresenter()
    ) {
        self.sessionManager = sessionManager
        self.toastPresenter = toastPresenter
    }

    /// Editing either field clears the message.
    ///
    /// Both handlers do it, so "changing what you typed dismisses the block" is a property of the
    /// state update rather than a separate rule someone has to remember to apply.
    func onEmailChange(_ email: String) {
        uiState.email = email
        uiState.message = nil
    }

    func onPasswordChange(_ password: String) {
        uiState.password = password
        uiState.message = nil
    }

    /// Signs in with what has been typed.
    ///
    /// On failure this deliberately leaves ``LoginUiState/email`` alone and navigates nowhere — the
    /// address someone just typed survives every failure, and nothing leaves a screen still showing
    /// an unexplained problem.
    func signIn() async {
        guard uiState.canSubmit else { return }
        let (email, password) = (uiState.email, uiState.password)
        beginSubmitting()

        do {
            let result = try await sessionManager.signIn(email: email, password: password)
            switch onEnum(of: result) {
            case .success(let success):
                // Nothing navigates: the root swaps the tree when SessionState changes. The
                // greeting goes through the toast presenter because this ViewModel is about to be
                // destroyed by that swap.
                if let signedIn = success.data as? SessionState.SignedIn {
                    toastPresenter.show(
                        AR.strings().auth_toast_welcome_back.localized(signedIn.player.firstName)
                    )
                }
                uiState.isSubmitting = false

            case .failure(let failure):
                // Only a rejected credential gets the designed block. A dropped connection or a
                // disabled account is a different problem, and saying "that email and password
                // don't match" about it would be a lie.
                report(failure.error, block: failure.error.isInvalidCredentials ? .signInFailed : nil)
            }
        } catch {
            report(error.asTeeTimeCaddieException(), block: nil)
        }
    }

    /// Creates the account, then moves to the profile step.
    ///
    /// An address that already has an account is reported here rather than after the profile form,
    /// which is the whole reason the Firebase account is created from this screen.
    ///
    /// - Parameter onCreated: Invoked with the typed address once the account exists.
    func createAccount(onCreated: (String) -> Void) async {
        guard uiState.canSubmit else { return }
        let (email, password) = (uiState.email, uiState.password)
        beginSubmitting()

        do {
            let result = try await sessionManager.startSignUp(email: email, password: password)
            switch onEnum(of: result) {
            case .success:
                uiState.isSubmitting = false
                onCreated(email)
            case .failure(let failure):
                report(failure.error, block: failure.error.isEmailInUse ? .emailInUse(email: email) : nil)
            }
        } catch {
            report(error.asTeeTimeCaddieException(), block: nil)
        }
    }

    /// The "Sign in instead" action on an ``AuthMessage/emailInUse(email:)`` block.
    ///
    /// Clears the password and the block but **keeps the email**: the address was right, it is the
    /// intent that changes. The person is already on the screen that signs in, so nothing moves.
    func onMessageAction() {
        uiState.password = ""
        uiState.message = nil
    }

    private func beginSubmitting() {
        uiState.isSubmitting = true
        uiState.message = nil
    }

    /// Ends a submission and says what went wrong.
    ///
    /// A failure the design draws a `block` for is shown on the screen, where it persists until the
    /// person edits a field. Everything else goes to the toast in its own words — the design has no
    /// banner for it, and showing nothing at all would read as the button not working.
    private func report(_ error: any TeeTimeCaddieException, block: AuthMessage?) {
        if block == nil {
            toastPresenter.show(error.displayMessage.localized(args: error.messageArgs))
        }
        uiState.isSubmitting = false
        uiState.message = block
    }
}

private extension TeeTimeCaddieException {
    var isInvalidCredentials: Bool { (self as? AuthException)?.error == .invalidCredentials }
    var isEmailInUse: Bool { (self as? AuthException)?.error == .emailInUse }
}
