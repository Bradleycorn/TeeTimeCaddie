//
//  TabNavStack.swift
//  Native-iOS
//
//  Created by Bradley Ball on 9/14/25.
//
import SwiftUI

/// A view that creates a NavigationStack for a specific tab with proper destination routing.
///
/// `TabNavStack` wraps each tab's content in a SwiftUI NavigationStack and configures
/// the navigation destination routing for all destination types in the app.
///
/// ## Navigation Stack Management
/// - Uses the Navigator's backstack binding for the specified tab
/// - Displays the tab's root view as the NavigationStack's root content
/// - Routes navigation destinations through a centralized switch statement
///
/// ## Destination Routing
/// The view handles navigation destination routing through `navigationDestination(for:)`,
/// which maps `AnyCdiNavKey` instances to their corresponding views. This is where
/// new destination types must be added when creating new features.
///
/// ## Tab Independence
/// Each TabNavStack operates independently, allowing users to build separate
/// navigation histories in each tab without affecting other tabs.
///
/// ## Usage
/// TabNavStack is used internally by AppTabView to create navigation stacks:
/// ```swift
/// TabNavStack(for: .races, navigator)
/// ```
///
/// ## Adding New Destination Types
/// When adding new destination enums, they must be added to the switch statement
/// in the `navigationDestination` modifier:
/// ```swift
/// case let navKey as NewFeatureDestinations:
///     navKey.destinationView(navigator)
/// ```
struct TabNavStack: View {
    /// The Navigator instance that manages navigation state
    private let navigator: Navigator
    
    /// The specific tab this NavigationStack represents
    private let tab: AppTabs
    
    /// Creates a NavigationStack for the specified tab.
    ///
    /// - Parameters:
    ///   - tab: The tab this NavigationStack will represent
    ///   - navigator: The Navigator instance managing navigation state
    init(for tab: AppTabs, _ navigator: Navigator,) {
        self.navigator = navigator
        self.tab = tab
    }

    
    var body: some View {
        NavigationStack(path: navigator.backstack(for: tab)) {
            // Display the tab's root view
            tab.destinationView(navigator)
                .navigationDestination(for: AnyTtcNavKey.self) { key in
                    // Route navigation destinations to their corresponding views
                    // This switch statement must be updated when adding new destination types
                    switch key.wrapped {
                    case let navKey as TeeTimesDestinations:
                        navKey.destinationView(navigator)
                    default:
                        fatalError("Unhandled navigation key: \(key)")
                    }
                }
        }
    }
}
