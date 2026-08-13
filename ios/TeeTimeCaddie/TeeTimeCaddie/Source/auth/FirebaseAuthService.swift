//
//  FirebaseAuthService.swift
//  TeeTimeCaddie
//
//  Platform (iOS) implementation of the shared AuthService interface, backed by the native
//  Firebase Auth SDK. Created by the app and passed into the SDK during initialization, so the
//  shared code carries no Firebase dependency. Native Firebase errors are classified into the
//  shared AuthErrors taxonomy here.
//

import Foundation
import TeeTimeCaddieKit
import FirebaseAuth

class FirebaseAuthService: AuthService {

    private let auth: Auth

    init(useEmulator: Bool) {
        auth = Auth.auth()
        if useEmulator {
            auth.useEmulator(withHost: Self.emulatorHost, port: Self.emulatorPort)
        }
    }

    var currentUserId: String? { auth.currentUser?.uid }
    var currentUserDisplayName: String? { auth.currentUser?.displayName }

    // Suspend requirements are exposed by SKIE with a `__` prefix (see FirebaseFirestoreClient).
    func __signIn(email: String, password: String) async throws -> AuthResult {
        do {
            let result = try await auth.signIn(withEmail: email, password: password)
            return AuthResultSuccess(userId: result.user.uid)
        } catch {
            return AuthResultFailure(error: TeeTimeCaddieKit.AuthErrors.invalidCredentials)
        }
    }

    func __register(email: String, password: String) async throws -> AuthResult {
        do {
            let result = try await auth.createUser(withEmail: email, password: password)
            return AuthResultSuccess(userId: result.user.uid)
        } catch let error as NSError {
            return AuthResultFailure(error: mapRegisterError(error))
        }
    }

    func __updateDisplayName(name: String) async throws {
        guard let user = auth.currentUser else { return }
        let request = user.createProfileChangeRequest()
        request.displayName = name
        try await request.commitChanges()
    }

    func __refreshToken() async throws -> KotlinBoolean {
        guard let user = auth.currentUser else { return KotlinBoolean(bool: false) }
        do {
            _ = try await user.getIDTokenResult(forcingRefresh: true)
            return KotlinBoolean(bool: true)
        } catch {
            return KotlinBoolean(bool: false)
        }
    }

    func signOut() {
        try? auth.signOut()
    }

    func observeAuthState(onChange: @escaping (String?) -> Void) -> TeeTimeCaddieKit.Cancellable {
        let handle = auth.addStateDidChangeListener { _, user in
            onChange(user?.uid)
        }
        return AuthStateCancellable(auth: auth, handle: handle)
    }

    private func mapRegisterError(_ error: NSError) -> TeeTimeCaddieKit.AuthErrors {
        switch error.code {
        case AuthErrorCode.emailAlreadyInUse.rawValue: return TeeTimeCaddieKit.AuthErrors.userExists
        case AuthErrorCode.invalidEmail.rawValue: return TeeTimeCaddieKit.AuthErrors.invalidEmail
        case AuthErrorCode.weakPassword.rawValue: return TeeTimeCaddieKit.AuthErrors.weakPassword
        default: return TeeTimeCaddieKit.AuthErrors.regDefault
        }
    }

    private static let emulatorHost = "127.0.0.1"
    private static let emulatorPort = 9099
}

private class AuthStateCancellable: TeeTimeCaddieKit.Cancellable {
    private let auth: Auth
    private let handle: AuthStateDidChangeListenerHandle
    init(auth: Auth, handle: AuthStateDidChangeListenerHandle) {
        self.auth = auth
        self.handle = handle
    }
    func cancel() { auth.removeStateDidChangeListener(handle) }
}
