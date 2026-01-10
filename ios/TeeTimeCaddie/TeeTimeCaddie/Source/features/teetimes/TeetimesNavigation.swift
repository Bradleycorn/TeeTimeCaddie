//
//  TeetimesNavigation.swift
//  TeeTimeCaddie
//
//  Created by Bradley Ball on 1/9/26.
//
import SwiftUI

enum TeeTimesDestinations: @MainActor TtcNavKey {
    case teeTimesList
    
    @ViewBuilder
    func destinationView(_ navigator: Navigator) -> some View {
        switch self {
        case .teeTimesList:
            TeeTimesListScreen()
        }
    }
}


extension Navigator {
    func navigateToTeeTimesTab(clearBackStack: Bool = false) {
        self.navigate(to: AppTabs.teeTimes, clearBackStack: clearBackStack)
    }
    
    func navigateToTeeTimes() {
        self.navigate(to: TeeTimesDestinations.teeTimesList)
    }
}
