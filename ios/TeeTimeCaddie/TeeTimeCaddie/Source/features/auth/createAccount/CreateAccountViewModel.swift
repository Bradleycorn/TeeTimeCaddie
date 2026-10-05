//
//  CreateAccountViewModel.swift
//  TeeTimeCaddie
//

import Foundation
import SwiftUI
import TeeTimeCaddieKit

/// State of the profile step of creating an account.
///
/// A struct rather than a sealed enum because its dimensions are **orthogonal**: a phone number
/// already in use produces a message block *and* an inline field error at the same time, and
/// exclusive states cannot express that.
struct CreateAccountUiState {
    var email: String = ""
    var name: String = ""

    /// Digits only. Grouping is applied on the way to the field by ``PhoneNumber``, never stored.
    var phoneDigits: String = ""

    /// The chosen photo, ready to draw.
    var photo: Image?

    /// The same photo as bytes, ready to upload.
    var photoData: Data?

    var phoneError: String?
    var message: AuthMessage?
    var isSubmitting: Bool = false

    var hasPhoto: Bool { photo != nil }

    var canSubmit: Bool {
        !name.trimmingCharacters(in: .whitespaces).isEmpty
            && phoneDigits.count == PhoneNumber.length
            && !isSubmitting
    }
}

/// `Image` is not `Equatable`, so the synthesised conformance is unavailable.
///
/// Comparing ``photoData`` stands in for it: the two are set together and the bytes are what
/// actually changed. Needed so ``CreateAccountContent`` previews stay pure value construction.
extension CreateAccountUiState: Equatable {
    static func == (lhs: Self, rhs: Self) -> Bool {
        lhs.email == rhs.email
            && lhs.name == rhs.name
            && lhs.phoneDigits == rhs.phoneDigits
            && lhs.photoData == rhs.photoData
            && lhs.phoneError == rhs.phoneError
            && lhs.message == rhs.message
            && lhs.isSubmitting == rhs.isSubmitting
    }
}

/// The Swift twin of `CreateAccountViewModel.kt` — same state, same method names.
@MainActor
@Observable
final class CreateAccountViewModel {

    private(set) var uiState: CreateAccountUiState

    private let sessionManager: SessionManager
    private let toastPresenter: TtcToastPresenter

    /// Whether sign-up finished successfully.
    ///
    /// Guards ``abandonSignUp()`` against the one case where leaving this screen must *not* clean
    /// up: completing sign-up also removes the screen, because the whole auth tree is replaced once
    /// `SessionState` becomes `SignedIn`. `SessionManager.abandonSignUp` will not delete a
    /// provisioned account — but it signs out unconditionally, which would bounce someone straight
    /// back out of the account they just made.
    private var didCompleteSignUp = false

    init(
        email: String,
        sessionManager: SessionManager = AuthModule.shared.sessionManager(),
        toastPresenter: TtcToastPresenter = AppModule.shared.toastPresenter()
    ) {
        self.uiState = CreateAccountUiState(email: email)
        self.sessionManager = sessionManager
        self.toastPresenter = toastPresenter
    }

    func onNameChange(_ name: String) {
        uiState.name = name
        uiState.message = nil
    }

    /// Accepts digits only, capped at a full number.
    ///
    /// Clears the inline error and the message block in the same update, so the two treatments of a
    /// phone problem can never drift out of step with each other.
    func onPhoneChange(_ phone: String) {
        uiState.phoneDigits = PhoneNumber.digits(phone)
        uiState.phoneError = nil
        uiState.message = nil
    }

    /// Tap-to-add, tap-to-remove.
    func onPhotoPicked(image: Image?, data: Data?) {
        uiState.photo = image
        uiState.photoData = data
    }

    func removePhoto() {
        uiState.photo = nil
        uiState.photoData = nil
    }

    /// Saves the profile, completing sign-up.
    ///
    /// Success navigates nowhere: the resulting `SessionState.SignedIn` swaps the whole tree. The
    /// greeting goes through the toast presenter for the same reason — this ViewModel does not
    /// survive it.
    func submit() async {
        guard uiState.canSubmit else { return }
        let state = uiState
        uiState.isSubmitting = true
        uiState.message = nil
        uiState.phoneError = nil

        do {
            let result = try await sessionManager.completeSignUp(
                name: state.name.trimmingCharacters(in: .whitespaces),
                phone: state.phoneDigits,
                photo: state.photoData?.toKotlinByteArray()
            )

            switch onEnum(of: result) {
            case .success(let success):
                didCompleteSignUp = true
                toastPresenter.show(
                    AR.strings().auth_toast_account_created.localized(success.data.firstName)
                )
                uiState.isSubmitting = false
            case .failure(let failure):
                report(failure.error)
            }
        } catch {
            report(error.asTeeTimeCaddieException())
        }
    }

    /// The "Sign in instead" action on a phone-in-use block.
    func onMessageAction() {
        abandonSignUp()
    }

    /// Abandons sign-up, deleting the account that has no profile.
    ///
    /// Called from the screen's back-navigation handler, so it covers every way out: the back
    /// button, the swipe gesture, and a programmatic pop.
    ///
    /// Nothing is awaited — `SessionManager` runs this in a scope that outlives the view, precisely
    /// because the act that triggers it is what tears the view down.
    func abandonSignUp() {
        guard !didCompleteSignUp else { return }
        sessionManager.abandonSignUp(reason: "back")
    }

    /// Ends a submission and says what went wrong.
    ///
    /// A phone number already in use is the one failure with a designed treatment, and it gets
    /// both: the block explains and offers a way out, the field marks what to change. Everything
    /// else goes to the toast in its own words.
    private func report(_ error: any TeeTimeCaddieException) {
        let playerError = error as? PlayerException
        let isPhoneInUse = playerError?.error == .phoneInUse

        if !isPhoneInUse {
            toastPresenter.show(error.displayMessage.localized(args: error.messageArgs))
        }

        uiState.isSubmitting = false
        uiState.message = isPhoneInUse ? .phoneInUse : nil
        uiState.phoneError = playerError?.inlineMessage?.localized()
    }
}
