//
//  ProfileNavigation.swift
//  TeeTimeCaddie
//

import SwiftUI
import TeeTimeCaddieKit

/// Destinations within the Profile tab.
///
/// The Swift twin of `ProfileNavigation.kt`. One case for now; the enum exists so the tab routes
/// through the same mechanism as every other feature rather than being special-cased.
enum ProfileDestinations: @MainActor TtcNavKey {
    case profile

    @ViewBuilder
    func destinationView(_ navigator: any Navigator) -> some View {
        switch self {
        case .profile:
            // Signing out is deliberately not a navigation callback: SessionManager is what
            // changes, and the root branches on the resulting SessionState, so the tab tree is
            // replaced without anyone navigating.
            ProfileScreen()
                .navigationTitle(PR.strings().profile_title.desc().localized())
                .navigationBarTitleDisplayMode(.inline)
        }
    }
}

extension Navigator {
    func navigateToProfileTab(clearBackStack: Bool = false) {
        self.navigate(to: AppTabs.profile, clearBackStack: clearBackStack)
    }
}
