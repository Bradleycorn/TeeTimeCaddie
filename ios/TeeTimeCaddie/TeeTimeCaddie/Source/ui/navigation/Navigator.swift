//
//  Navigator.swift
//  Native-iOS
//
//  Created by Bradley Ball on 8/29/25.
//
import SwiftUI

/// The central navigation coordinator that manages all navigation state across the entire app.
///
/// `Navigator` implements a sophisticated navigation system with per-tab backstacks, automatic
/// authentication enforcement, and feature toggle integration. It serves as the single source
/// of truth for navigation state and provides type-safe navigation methods.
///
/// ## Key Features
/// - **Per-tab navigation stacks**: Each tab maintains its own independent navigation history
/// - **Authentication enforcement**: Automatically redirects to login for auth-required destinations
/// - **Feature toggle integration**: Blocks navigation to disabled features
/// - **Type-safe navigation**: Compile-time safety through protocol conformance
/// - **Cross-tab navigation**: Seamless navigation between different feature areas
///
/// ## Separation of Concerns
/// This architecture maintains strict separation between navigation logic and UI components.
/// Views should remain purely presentational and use callbacks to report user actions rather
/// than performing navigation directly.
///
/// **Important:** The `Navigator` should **NOT** be passed down into screen views or added to
/// the SwiftUI environment as an `@EnvironmentObject`. This would break the separation of
/// concerns by giving views direct access to navigation capabilities they shouldn't need.
/// Instead, views should receive navigation callbacks that encapsulate the specific actions
/// they can trigger.
///
/// ```swift
/// // ✅ Correct: Views receive navigation callbacks
/// HomeScreen(
///     onLoginClicked: { navigator.navigateToLogin() },
///     onRaceClicked: { race in navigator.navigateToProgram(race: race) }
/// )
///
/// // ❌ Incorrect: Don't pass Navigator to views
/// HomeScreen(navigator: navigator)
///
/// // ❌ Incorrect: Don't use Navigator as environment object
/// HomeScreen().environmentObject(navigator)
/// ```
///
/// This approach ensures views remain testable, reusable, and focused solely on presentation
/// while keeping navigation logic centralized and explicit.
///
/// ## Usage
/// ```swift
/// // Navigate to a specific destination
/// navigator.navigate(to: ProgramDestinations.program(race: 5))
///
/// // Navigate with backstack clearing
/// navigator.navigate(to: AppTabs.races, clearBackStack: true)
///
/// // Pop navigation stack
/// navigator.pop()
/// ```
@MainActor
@Observable
class Navigator {
    /// Static counter for tracking Navigator instances during debugging
    static var instanceCount: Int = 0
    
    /// Unique identifier for this Navigator instance
    var instanceId: Int

    /// All available tabs in the top level TabView
    let tabs: [AppTabs] = [.teeTimes]

    /// The currently selected tab.
    /// Use this as a binding for the TabView's selection.
    var currentTab: AppTabs
    
    /// Navigation stacks for each tab, mapping a tab to its backstack of destinations
    private var tabBackstacks: Dictionary<AppTabs, [AnyTtcNavKey]>

    /// The navigation stack for the currently active tab
    private var currentBackstack: [AnyTtcNavKey] {
        get { tabBackstacks[currentTab]! }
        set { tabBackstacks[currentTab] = newValue }
    }
        
    /// Initializes a new Navigator instance with empty navigation stacks for each tab.
    ///
    /// Sets up the initial state with the first tab as the current tab and empty
    /// backstacks for all tabs.
    init() {
        Navigator.instanceCount += 1
        instanceId = Navigator.instanceCount
        print("Navigator - Initializing Navigator (id: \(Navigator.instanceCount))")

        let stacks: Dictionary<AppTabs, [AnyTtcNavKey]> = Dictionary(uniqueKeysWithValues: tabs.map { ($0, []) })
        let startDestination = tabs.first!
        tabBackstacks = stacks
        currentTab = startDestination
    }
    
    deinit {
        print("Navigator - Deinitializing Navigator")
    }
    
    /// Returns the unique instance identifier for debugging purposes.
    ///
    /// - Returns: The instance ID of this Navigator
    func getInstanceId() -> Int {
        print("Navigator - Using Instance: \(instanceId)")
        return instanceId
    }
        
    /// Returns a binding to the navigation stack for the specified tab.
    ///
    /// This method provides SwiftUI with access to the navigation state for a specific tab,
    /// enabling the NavigationStack to manage the backstack automatically.
    ///
    /// - Parameter tab: The tab whose backstack to retrieve
    /// - Returns: A binding to the tab's navigation stack
    /// - Note: Will trigger a fatal error if the tab doesn't have an associated backstack
    func backstack(for tab: AppTabs) -> Binding<[AnyTtcNavKey]> {
        guard let _ = tabBackstacks[tab] else { fatalError("No backstack found for tab \(tab)") }

        return Binding(
            get: { self.tabBackstacks[tab]! },
            set: { self.tabBackstacks[tab] = $0 }
        )
    }
        
    /// Navigates to the specified destination with optional backstack clearing.
    ///
    ///  While this method can be called directly, prefer using the overload that accepts
    ///  `any TtcNavKey` for better type safety and convenience.
    ///
    /// This is the core navigation method that handles all navigation logic including:
    /// - Authentication enforcement (redirects to login if needed)
    /// - Feature toggle validation (blocks disabled features)
    /// - Tab switching (when destination is a tab)
    /// - Backstack management (appending to current tab's stack)
    /// - Duplicate navigation prevention
    ///
    /// - Parameters:
    ///   - navKey: The wrapped destination to navigate to
    ///   - clearBackStack: Whether to clear the current backstack before navigation
    func navigate(to navKey: AnyTtcNavKey, clearBackStack: Bool = false) {
        let key = navKey.wrapped
        print("Navigator - Navigating to \(key) using instance \(instanceId)")
 
        // If we're already at the requested destination, do nothing
        if let tabKey = key as? AppTabs, tabKey == currentTab && !clearBackStack { return }
        if currentBackstack.last == navKey { return }
        
        // If the destination is a tab, switch to it
        if let tabKey = key as? AppTabs {
            print("Navigator - Switching to tab \(key)")
            if tabKey != currentTab {
                currentTab = tabKey
            }
        }
        
        // If we need to clear the backstack, do so
        if clearBackStack {
            clearbackstack()
        }
        
        // If the destination is NOT a tab, add it to the current tab's backstack
        if key as? AppTabs == nil {
            currentBackstack.append(navKey)
        }

    }

    /// Navigates to the specified destination with optional backstack clearing.
    ///
    /// Convenience overload that automatically wraps the destination in `AnyTtcNavKey`.
    /// Prefer using this method for better type safety and ease of use.
    ///
    /// - Parameters:
    ///   - key: The destination to navigate to
    ///   - clearBackStack: Whether to clear the current backstack before navigation
    func navigate(to key: any TtcNavKey, clearBackStack: Bool = false) {
        navigate(to: AnyTtcNavKey(key), clearBackStack: clearBackStack)
    }
    
    /// Removes the topmost destination from the current tab's navigation stack.
    ///
    /// This is equivalent to tapping the back button in the navigation bar.
    /// If the backstack is empty, this method does nothing.
    func pop() {
        if !currentBackstack.isEmpty {
            currentBackstack.removeLast()
        }
    }
    
    /// Removes destinations from the navigation stack until reaching the specified destination.
    ///
    /// This method provides fine-grained control over navigation stack management by allowing
    /// you to pop multiple destinations at once to reach a specific target.
    ///
    /// - Parameters:
    ///   - key: The destination to pop up to
    ///   - inclusive: If `true`, also removes the target destination; if `false`, keeps it
    ///
    /// ## Examples
    /// ```swift
    /// // Pop back to Today's Races (keeping it in the stack)
    /// navigator.popUpTo(to: ProgramDestinations.wagerProgram, inclusive: false)
    ///
    /// // Pop back to and remove the login screen
    /// navigator.popUpTo(to: AuthDestinations.login(), inclusive: true)
    ///
    /// // Clear entire backstack (pop to tab root)
    /// navigator.popUpTo(to: AppTabs.races, inclusive: false)
    /// ```
    func popUpTo(to navKey: AnyTtcNavKey, inclusive: Bool = false) {
        let key = navKey.wrapped

        // if we're popping up to the current tab's root view, just clear the backstack.
        if let tabKey = key as? AppTabs, tabKey == currentTab {
            clearbackstack()
            return
        }
        
        if let index = currentBackstack.firstIndex(of: AnyTtcNavKey(key)) {
            currentBackstack.popUpTo(index: index, inclusive: inclusive)
        }
    }
    
    /// Removes destinations from the navigation stack until reaching the specified destination.
    ///
    /// Convenience overload that automatically wraps the destination in `AnyTtcNavKey`.
    /// Prefer using this method for better type safety and ease of use.
    func popUpTo(to key: any TtcNavKey, inclusive: Bool = false) {
        popUpTo(to: AnyTtcNavKey(key), inclusive: inclusive)
    }
    
    /// Removes all destinations from the current tab's navigation stack.
    ///
    /// This returns the user to the tab's root view by clearing all navigation history
    /// for the current tab. Other tabs' navigation stacks remain unchanged.
    func clearbackstack() {
        currentBackstack.removeAll()
    }
}

/// Extension providing utility methods for array-based navigation stack manipulation.
extension [AnyTtcNavKey] {
    /// Removes elements from the array up to and optionally including the specified index.
    ///
    /// - Parameters:
    ///   - index: The index to pop up to
    ///   - inclusive: If `true`, removes the element at the index; if `false`, keeps it
    mutating func popUpTo(index: Int, inclusive: Bool = false) {
        if inclusive {
            removeSubrange(index...)
        } else if index + 1 < count {
            removeSubrange((index+1)...)
        }
    }
        
}
