//
//  TabsNavView.swift
//  Native-iOS
//
//  Created by Bradley Ball on 9/14/25.
//
import SwiftUI

/// Top-level navigation for the **signed-in** app: the tab bar and a stack per tab.
///
/// `TabsNavView` creates the bottom tab bar interface.
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
///
/// The counterpart for the signed-out half is ``AuthNavView``. `TeeTimeCaddieView` picks between
/// the two by branching on `SessionState` — they are alternatives, never destinations of one
/// another. The Android twin is `NavBarNavDisplay`.
struct TabsNavView: View {

    /// The tab bar's glyph size, matching UIKit's own.
    private static let iconSize: CGFloat = 24

    /// The TabNavigator that manages navigation state and tab switching.
    ///
    /// Created here rather than passed in from the root. Where it is owned decides how long the tab
    /// stacks live: owned here, signing out discards them, instead of leaving a signed-out player's
    /// Games history waiting behind the credentials screen. The Android twin, `NavBarNavDisplay`,
    /// does the same.
    @State private var navigator = TabNavigator()
        
    var body: some View {
        TabView(selection: $navigator.currentTab) {
            ForEach(navigator.tabs, id: \.self) { tab in
                
                    // Display normal tab content with navigation stack
                    TabNavStack(for: tab, navigator)
                    .tabItem {
                        Label(tab.iconText, icon: tab.icon)
                    }

                // Note: Tabs with disabled features (result == .featureDisabled) are not rendered,
                // effectively hiding them from the tab bar
            }
        }
    }
}
