//
//  TeeTimeCaddieApp.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 5/20/23.
//

import SwiftUI
import FirebaseCore
import FirebaseAuth
import TeeTimeCaddieKit
import KMPNativeCoroutinesAsync

import FirebaseAnalyticsSwift

@main
struct TeeTimeCaddieApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    @Environment(\.scenePhase) var scenePhase
    
    @StateObject
    var appState: TeeTimeCaddieAppState = TeeTimeCaddieAppState()
            
    var body: some Scene {
        WindowGroup {
            UserInterface(
                state: appState.uiState,
                onLoginClick: { appState.setUiState(to: .LOGIN) },
                onRegisterClick: { appState.setUiState(to: .REGISTRATION) })
                    .task { await appState.observeAuthState() }
                    .onChange(of: scenePhase) { phase in
                        if (phase == .active) {
                            Task { await asyncResult(for: AuthModule.shared.authRepository().refreshAuthentication()) }
                        }
                    }
                    .animation(.default, value: appState.uiState)
        }
    }
}


struct UserInterface: View {
    private let state: UiState
    private let onLoginClick: ()->Void
    private let onRegisterClick: ()->Void
    
    init(
        state: UiState,
        onLoginClick: @escaping () -> Void = {},
        onRegisterClick: @escaping () -> Void = {}) {
 
        self.state = state
        self.onLoginClick = onLoginClick
        self.onRegisterClick = onRegisterClick
    }
    
    var body: some View {
        switch(state) {
        case .APP:
            Text("Show the app")
                .onTapGesture { try? Auth.auth().signOut() }
            
        case .LOGIN:
            LoginScreen(onRegisterClick: onRegisterClick)
                .transition(.move(edge: .trailing))
        case .REGISTRATION:
            RegistrationScreen(onLoginClick: onLoginClick)
                .transition(.move(edge: .trailing))
        }
    }
}

