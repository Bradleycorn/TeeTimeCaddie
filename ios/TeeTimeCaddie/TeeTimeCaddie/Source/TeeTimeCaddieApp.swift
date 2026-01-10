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

import FirebaseAnalyticsSwift

@main
struct TeeTimeCaddieApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    @Environment(\.scenePhase) var scenePhase
    
    @StateObject
    var appState: TeeTimeCaddieAppState = TeeTimeCaddieAppState()
         
    @State
    private var navigator: Navigator = Navigator()
    
    var body: some Scene {
        WindowGroup {
            UserInterface(
                state: appState.uiState,
                navigator: navigator,
                onLoginClick: { appState.setUiState(to: .LOGIN) },
                onRegisterClick: { appState.setUiState(to: .REGISTRATION) })
                    .task { await appState.observeAuthState() }
                    .onChange(of: scenePhase) { _, newPhase in
                        if (newPhase == .active) {
                            Task { try? await AuthModule.shared.authRepository().refreshAuthentication() }
                        }
                    }
                    .animation(.default, value: appState.uiState)
        }
    }
}


struct UserInterface: View {
    private let state: AppUiState
    private let navigator: Navigator
    private let onLoginClick: ()->Void
    private let onRegisterClick: ()->Void
    
    init(
        state: AppUiState,
        navigator: Navigator,
        onLoginClick: @escaping () -> Void = {},
        onRegisterClick: @escaping () -> Void = {}) {
 
        self.state = state
        self.navigator = navigator
        self.onLoginClick = onLoginClick
        self.onRegisterClick = onRegisterClick
    }
    
    var body: some View {
        TeeTimeCaddieTheme {
            switch(state) {
                case .APP:
                    //TODO: When we need to have tabs, swith to AppTabView here.
                    TabNavStack(for: .teeTimes, navigator)
                    //TeeTimesNavStack(navigator: navigator)
                case .LOGIN:
                    LoginScreen(onRegisterClick: onRegisterClick)
                        .transition(.move(edge: .trailing))
                    
                case .REGISTRATION:
                    RegistrationScreen(onLoginClick: onLoginClick)
                        .transition(.move(edge: .trailing))
            }
        }
    }
}

