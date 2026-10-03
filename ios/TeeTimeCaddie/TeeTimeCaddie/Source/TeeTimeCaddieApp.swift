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
/// Each branch's top-level navigation lives in its own file beside the other, in
/// `ui/navigation/Views`: ``AuthNavView`` and ``TabsNavView``.
///
/// - `.signedIn`: the tabbed app
/// - `.signedOut` / `.profileIncomplete`: the auth flow — a Firebase account with no profile yet
///   still belongs to sign-up, not to the tabs
/// - `.loading`: nothing, briefly, while a persisted session is restored
///
/// - SeeAlso: ``TeeTimeCaddieApp``, ``TeeTimeCaddieAppState``, ``AuthNavView``, ``TabsNavView``
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
                    TabsNavView(navigator)
                case .signedOut, .profileIncomplete:
                    AuthNavView(sessionState: appState.sessionState)
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
