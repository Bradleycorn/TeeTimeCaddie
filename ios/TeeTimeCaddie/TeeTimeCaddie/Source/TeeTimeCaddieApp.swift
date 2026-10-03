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
/// **Auth and the tabs are alternatives, not destinations.** The view branches on `SessionState`
/// rather than navigating to a login screen, so there is no route by which a signed-out person's
/// tab backstacks survive underneath the credentials screen. The Android twin, `TeeTimeCaddieApp`,
/// makes the same decision for the same reason.
///
/// - `.signedIn`: the tabbed app
/// - `.signedOut` / `.profileIncomplete`: the auth flow — a Firebase account with no profile yet
///   still belongs to sign-up, not to the tabs
/// - `.loading`: nothing, briefly, while a persisted session is restored
///
/// - SeeAlso: ``TeeTimeCaddieApp``, ``TeeTimeCaddieAppState``, ``AuthNavigationStack``
fileprivate struct TeeTimeCaddieView: View {
    @State private var appState = TeeTimeCaddieAppState()
    @State private var navigator = Navigator()

    /// Applied once, at the root — above the auth/tabs branch, because the view that knows "you are
    /// signed in" is destroyed by the very change that signing in triggers.
    @State private var toastPresenter = TtcToastPresenter()

    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        TeeTimeCaddieTheme {
            Group {
                switch onEnum(of: appState.sessionState) {
                case .signedIn:
                    AppTabView(navigator)
                case .signedOut, .profileIncomplete:
                    AuthNavigationStack(sessionState: appState.sessionState)
                        .transition(.move(edge: .trailing))
                case .loading:
                    // Seeded only when a session probably exists, so this is a frame or two.
                    ContentLoadingIndicator()
                }
            }
            .ttcToast(toastPresenter)
        }
        .task { await appState.observeSessionState() }
        .onChange(of: scenePhase) { _, newPhase in
            if (newPhase == .active) {
                Task { await appState.refreshSession() }
            }
        }
        .animation(.default, value: appState.sessionState)
    }
}

/// The auth flow's own `NavigationStack`.
///
/// Separate from ``Navigator`` on purpose: auth is the alternative to the app, not a section of it,
/// and keeping its stack apart is what stops signing out from leaving a credentials screen stacked
/// on a stale tee-times history. The Android twin is `AuthNavDisplay`.
fileprivate struct AuthNavigationStack: View {
    let sessionState: SessionState

    @State private var path: [AuthDestinations] = []

    /// The email typed on the credentials screen.
    ///
    /// Owned here rather than by `LoginScreen`, so popping back from the profile step restores it
    /// even though the screen below was torn down. The Android twin gets this from the nav entry's
    /// own `ViewModelStore`; SwiftUI has no equivalent, so the container holds it.
    @State private var email = ""

    var body: some View {
        NavigationStack(path: $path) {
            LoginScreen(onCreateAccount: { typedEmail in
                email = typedEmail
                path.append(.createAccount(email: typedEmail))
            })
            .navigationDestination(for: AuthDestinations.self) { destination in
                switch destination {
                case .login:
                    LoginScreen(onCreateAccount: { _ in })
                case .createAccount(let email):
                    // No cleanup callback: the screen's own back-navigation handler owns that, so
                    // it runs for the system back button and the swipe gesture too — neither of
                    // which passes through here.
                    CreateAccountScreen(email: email, onBack: pop)
                        .navigationTitle(AR.strings().create_account_title.desc().localized())
                        .navigationBarTitleDisplayMode(.inline)
                }
            }
        }
        // A restored half-finished sign-up resumes at the profile step rather than at the
        // credentials screen, with the credentials screen still underneath to go back to.
        .onAppear {
            if case .profileIncomplete(let incomplete) = onEnum(of: sessionState), path.isEmpty {
                email = incomplete.email
                path = [.createAccount(email: incomplete.email)]
            }
        }
    }

    /// Pops the stack. Cleanup is the popped screen's own business.
    private func pop() {
        if !path.isEmpty { path.removeLast() }
    }
}
