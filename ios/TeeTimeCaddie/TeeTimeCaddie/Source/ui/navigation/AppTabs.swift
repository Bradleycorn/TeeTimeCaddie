//
//  AppTabs.swift
//
//  Created by Bradley Ball on 8/29/25.
//
import SwiftUI

/// Represents the top-level tabs in the application's tab bar interface.
///
/// `AppTabs` defines all possible main navigation destinations that appear in the bottom tab bar.
/// Each tab serves as the root of its own navigation stack and can have specific
/// authentication requirements and feature toggle controls.
///
/// This enum defines all possible tabs. Each affiliate provides its own array of AppTabs to define
/// the tabs that are shown for that affiliate.
///
/// This enum conforms to `TtcNavKey` to integrate with the navigation system, allowing
/// navigation to a tab using the `Navigator`, the same as you would navigate to any other destination.
///
/// ## Authentication
/// Tabs can require authentication by setting `authRequired` to `true`. When users
/// attempt to access auth-required tabs without being logged in, they are automatically
/// redirected to the login screen.
///
/// ## Feature Toggles
/// Tabs support feature toggles through the `featureToggle` property, allowing for
/// A/B testing and gradual feature rollouts.
///
/// ## Usage
/// AppTabs are used automatically by the navigation system. The `Navigator` manages
/// tab switching and maintains separate navigation stacks for each tab.
///
/// ```swift
/// // Tab switching is handled by Navigator
/// navigator.navigate(to: AppTabs.races)
/// ```
enum AppTabs: @MainActor TtcNavKey, Hashable {
    
    case teeTimes
        
    /// The icon to display in the tab bar for this tab.
    ///
    /// Uses either SF Symbols or custom assets depending on the tab.
    var icon: ImageResource {
        switch self {
        case .teeTimes: return .icon
        }
    }
    
    /// The text label to display in the tab bar for this tab.
    var iconText: String {
        switch self {
        case .teeTimes: return "Tee Times"
        }
    }
        
    /// Indicates whether this tab's feature is currently enabled.
    ///
    /// Uses the feature toggle system to determine if the tab should be available.
    /// When a feature is disabled, the tab will not appear in the tab bar.
//    var featureEnabled: Bool {
//        // TODO, make this a switch when we need to apply feature toggles
//        // to tabs.
//        self.featureToggle?.isEnabled ?? true
//    }
    
    /// Returns the root view for a tab with appropriate navigation setup.
    ///
    /// - Parameter navigator: The Navigator instance for handling navigation callbacks
    /// - Returns: The configured root view for this tab
    @MainActor
    @ViewBuilder
    func destinationView(_ navigator: Navigator) -> some View {
        switch self {
        case .teeTimes:
            TeeTimesDestinations.teeTimesList.destinationView(navigator)
                .navigationTitle(iconText)
                .navigationBarTitleDisplayMode(.inline)
        }
    }
}
