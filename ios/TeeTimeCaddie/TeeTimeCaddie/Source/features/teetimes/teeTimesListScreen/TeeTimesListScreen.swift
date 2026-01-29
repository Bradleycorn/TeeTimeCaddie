//
//  TeeTimesScreen.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 2/21/24.
//

import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

struct TeeTimesListScreen: View {
    let onAddTeeTimeClick: () -> Void

    @State
    private var viewModel = TeeTimesListScreenViewModel()

    var body: some View {
        Screen(AnalyticsScreen.TeeTimeList(viewName: self.viewName)) {
            TeeTimesListContent(uiState: viewModel.uiState)
        }
        .task { await viewModel.loadTeetimes() }
        .toolbar {
            Button(action: onAddTeeTimeClick) {
                Image(.Icons.calendarAdd)
            }
            .enabled(viewModel.addButtonEnabled)
        }
    }
}

fileprivate struct TeeTimesListContent: View {
    private var uiState: UiState<[TeeTime]>
    
    init(uiState: UiState<[TeeTime]>) {
        self.uiState = uiState
    }
        
    var body: some View {
        switch uiState {
            case .Loading:
                ContentLoadingIndicator()

            case .Empty:
                EmptyContent(
                    title: TTR.strings().empty_tee_times_title.desc().localized(),
                    message: TTR.strings().empty_tee_times_message.desc().localized(),
                    icon: .teeEmpty)

            case .Content(let list):
                TeeTimesList(list, onItemTapped: {teeTime in })
        }
    }
}

#Preview("Content") {
    TeeTimeCaddieTheme {
        TeeTimesListContent(uiState: .Content(TeeTimeKt.previewTeeTimeList))
    }
}

#Preview("Loading") {
    TeeTimeCaddieTheme {
        TeeTimesListContent(uiState: .Loading)
    }
}

#Preview("Empty") {
    TeeTimeCaddieTheme {
        TeeTimesListContent(uiState: .Empty)
    }
}
