//
//  TeetimesNavigation.swift
//  TeeTimeCaddie
//
//  Created by Bradley Ball on 1/9/26.
//
import SwiftUI
import TeeTimeCaddieKit

enum TeeTimesDestinations: @MainActor TtcNavKey {
    case teeTimesList
    case addTeeTime
    case editTeeTime(String)

    @ViewBuilder
    func destinationView(_ navigator: Navigator) -> some View {

        switch self {
        case .teeTimesList:
            TeeTimesListScreen(
                onAddTeeTimeClick: { navigator.navigateToAddTeeTime() },
                onTeeTimeClick: { teeTime in navigator.navigateToEditTeeTime(teeTime) }
            )
            .navigationTitle(TTR.strings().tee_times_title.desc().localized())
            .navigationBarTitleDisplayMode(.inline)

        case .addTeeTime:
            AddTeeTimeScreen(
                onBack: { navigator.pop() },
                onTeeTimeCreated: { navigator.pop() }
            )
            .navigationTitle(TTR.strings().add_tee_time.desc().localized())
            .navigationBarTitleDisplayMode(.inline)

        case .editTeeTime(let teeTimeId):
            EditTeeTimeScreen(
                teeTimeId: teeTimeId,
                onBack: { navigator.pop() },
                onTeeTimeSaved: { navigator.pop() }
            )
            .navigationTitle(TTR.strings().edit_tee_time.desc().localized())
            .navigationBarTitleDisplayMode(.inline)
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

    func navigateToAddTeeTime() {
        self.navigate(to: TeeTimesDestinations.addTeeTime)
    }

    func navigateToEditTeeTime(_ teeTime: TeeTime) {
        guard let teeTimeId = teeTime.id else { return }
        self.navigate(to: TeeTimesDestinations.editTeeTime(teeTimeId))
    }
}
