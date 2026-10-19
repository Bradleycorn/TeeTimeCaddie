//
//  TabNavStack.swift
//  Native-iOS
//
//  Created by Bradley Ball on 9/14/25.
//
import SwiftUI
import TeeTimeCaddieKit

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
/// ## Tab Bar Visibility
/// The tab bar shows only on a tab's root view. Every pushed destination hides it, which is the
/// same rule Android applies through `NavBarNavigator.isAtTopLevel`.
///
/// ## Tab Independence
/// Each TabNavStack operates independently, allowing users to build separate
/// navigation histories in each tab without affecting other tabs.
///
/// ## Usage
/// TabNavStack is used internally by TabsNavView to create navigation stacks:
/// ```swift
/// TabNavStack(for: .games, navigator, eventManager: eventManager)
/// ```
///
/// ## Adding New Destination Types
/// When adding new destination enums, they must be added to the switch statement
/// in the `navigationDestination` modifier:
/// ```swift
/// case let navKey as NewFeatureDestinations:
///     navKey.destinationView(navigator, eventManager: eventManager)
/// ```
struct TabNavStack: View {
    /// The Navigator instance that manages navigation state
    private let navigator: TabNavigator
    
    /// The specific tab this NavigationStack represents
    private let tab: AppTabs

    /// Handed to each destination, for the controls its navigation layer owns
    private let eventManager: EventManager
    
    /// Creates a NavigationStack for the specified tab.
    ///
    /// - Parameters:
    ///   - tab: The tab this NavigationStack will represent
    ///   - navigator: The TabNavigator instance managing navigation state
    ///   - eventManager: Handed to each destination, for the controls its navigation layer owns
    init(for tab: AppTabs, _ navigator: TabNavigator, eventManager: EventManager) {
        self.navigator = navigator
        self.tab = tab
        self.eventManager = eventManager
    }

    
    var body: some View {
        NavigationStack(path: navigator.backstack(for: tab)) {
            // Display the tab's root view
            tab.destinationView(navigator, eventManager: eventManager)
                .navigationDestination(for: AnyTtcNavKey.self) { key in
                    // Route navigation destinations to their corresponding views
                    // This switch statement must be updated when adding new destination types
                    Group {
                        switch key.wrapped {
                        case let navKey as GamesDestinations:
                            navKey.destinationView(navigator, eventManager: eventManager)
                        case let navKey as ProfileDestinations:
                            navKey.destinationView(navigator, eventManager: eventManager)
                        default:
                            fatalError("Unhandled navigation key: \(key)")
                        }
                    }
                    // Everything routed here is pushed on top of a tab's root, and the tab bar
                    // shows only on the root: the player backs out to the top level before
                    // switching tabs. One rule, applied once, rather than a flag per destination.
                    .toolbar(.hidden, for: .tabBar)
                }
        }
    }
}
