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

/// The main entry point for the TeeTimeCaddie iOS application.
///
/// This struct serves as a minimal shell whose primary responsibility is to register the
/// ``AppDelegate`` via `@UIApplicationDelegateAdaptor`. The AppDelegate is required for
/// Firebase configuration and handling Firebase-related lifecycle events (push notifications,
/// authentication URL handling, etc.).
///
/// ## Important: Initialization Order
///
/// In SwiftUI, `@State` properties on an `App` struct are initialized *before* the
/// `AppDelegate.didFinishLaunchingWithOptions` method is called. This creates a race condition
/// when those state objects depend on services (like Firebase) that are configured in the
/// AppDelegate.
///
/// To avoid this issue, all application state (including ``TeeTimeCaddieAppState`` and
/// ``Navigator``) is intentionally created inside ``TeeTimeCaddieView`` rather than here.
/// Since SwiftUI evaluates the `body` property lazily (after the AppDelegate has run),
/// state objects created within `body` or its child views are guaranteed to be initialized
/// after Firebase is configured.
///
/// ## Structure
///
/// - `TeeTimeCaddieApp`: Minimal entry point, holds only the AppDelegate adaptor
/// - ``TeeTimeCaddieView``: The actual root view that owns application state and renders the UI
///
/// - SeeAlso: ``AppDelegate``, ``TeeTimeCaddieView``, ``TeeTimeCaddieAppState``
@main
struct TeeTimeCaddieApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            TeeTimeCaddieView()
        }
    }
}


/// The root view of the TeeTimeCaddie application.
///
/// `TeeTimeCaddieView` owns and manages the core application state, including:
/// - ``TeeTimeCaddieAppState``: Tracks authentication state and determines which UI to show
/// - ``Navigator``: Manages navigation stacks for each tab
///
/// ## Why State Lives Here (Not in TeeTimeCaddieApp)
///
/// This view exists as a deliberate architectural choice to solve a SwiftUI/Firebase
/// initialization timing issue. In SwiftUI's `App` struct, `@State` properties are
/// initialized before `@UIApplicationDelegateAdaptor` triggers the AppDelegate's
/// `didFinishLaunchingWithOptions` method. Since ``TeeTimeCaddieAppState`` depends on
/// Firebase Auth (which must be configured first), creating it as a property of
/// `TeeTimeCaddieApp` would cause a crash or undefined behavior.
///
/// By moving state ownership here, we ensure that:
/// 1. The AppDelegate runs first and configures Firebase
/// 2. SwiftUI then evaluates `TeeTimeCaddieApp.body`
/// 3. `TeeTimeCaddieView` is created, and its `@State` properties are initialized
/// 4. At this point, Firebase is fully configured and safe to use
///
/// ## UI States
///
/// The view switches between three main states based on ``AppUiState``:
/// - `.REGISTRATION`: Shows the registration flow for new users
/// - `.LOGIN`: Shows the login screen for returning users
/// - `.APP`: Shows the main application content with navigation
///
/// - SeeAlso: ``TeeTimeCaddieApp``, ``TeeTimeCaddieAppState``, ``AppUiState``
fileprivate struct TeeTimeCaddieView: View {
    @State private var appState = TeeTimeCaddieAppState()
    @State private var navigator = Navigator()
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        TeeTimeCaddieTheme {
            switch(appState.uiState) {
                case .APP:
                    //TODO: When we need to have tabs, switch to AppTabView here.
                    TabNavStack(for: .teeTimes, navigator)
                case .LOGIN:
                    LoginScreen(onRegisterClick: { appState.setUiState(to: .REGISTRATION) })
                        .transition(.move(edge: .trailing))
                case .REGISTRATION:
                    RegistrationScreen(onLoginClick: { appState.setUiState(to: .LOGIN) })
                        .transition(.move(edge: .trailing))
            }
        }
        .task { await appState.observeAuthState() }
        .onChange(of: scenePhase) { _, newPhase in
            if (newPhase == .active) {
                Task { try? await AuthModule.shared.authRepository().refreshAuthentication() }
            }
        }
        .animation(.default, value: appState.uiState)
    }
}
