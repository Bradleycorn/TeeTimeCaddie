//
//  AppTabView.swift
//  Native-iOS
//
//  Created by Bradley Ball on 9/14/25.
//
import SwiftUI

/// The main tab interface for the application that displays enabled tabs and handles authentication.
///
/// `AppTabView` creates the bottom tab bar interface.
///
/// ## Authentication Handling
/// When a tab requires authentication and the user is not logged in:
/// - The tab displays a login screen instead of the tab's normal content
/// - After successful login, the user is returned to their original intended destination
/// - The login screen includes an option to cancel and return to the home tab
///
/// ## Feature Toggle Integration
/// Tabs controlled by disabled feature toggles are automatically hidden from the tab bar,
/// providing seamless A/B testing and feature rollout capabilities.
///
/// ## Navigation Integration
/// State for the TabView is provided by the passed in `Navigator`.
struct AppTabView: View {
    
    /// The Navigator instance that manages navigation state and tab switching
    @State private var navigator: Navigator
    
    /// Creates a new AppTabView with the specified Navigator instance.
    ///
    /// - Parameter navigator: The Navigator that manages tab state and navigation
    init(_ navigator: Navigator) {
        self.navigator = navigator
    }
        
    var body: some View {
        TabView(selection: $navigator.currentTab) {
            ForEach(navigator.tabs, id: \.self) { tab in
                
                    // Display normal tab content with navigation stack
                    TabNavStack(for: tab, navigator)
                    .tabItem { Label(tab.iconText, icon: tab.icon) }

                // Note: Tabs with disabled features (result == .featureDisabled) are not rendered,
                // effectively hiding them from the tab bar
            }
        }
    }
}
