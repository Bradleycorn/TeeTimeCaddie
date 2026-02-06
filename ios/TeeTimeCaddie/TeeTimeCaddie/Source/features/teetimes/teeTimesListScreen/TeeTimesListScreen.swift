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
    let onTeeTimeClick: (TeeTime) -> Void

    @State
    private var viewModel = TeeTimesListScreenViewModel()

    var body: some View {
        Screen(AnalyticsScreen.TeeTimeList(viewName: self.viewName)) {
            TeeTimesListContent(uiState: viewModel.uiState, onTeeTimeClick: { teeTime in
                viewModel.onTeeTimeClick()
                onTeeTimeClick(teeTime)
            })
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
    private var onTeeTimeClick: (TeeTime) -> Void

    init(uiState: UiState<[TeeTime]>, onTeeTimeClick: @escaping (TeeTime) -> Void) {
        self.uiState = uiState
        self.onTeeTimeClick = onTeeTimeClick
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
                TeeTimesList(list, onItemTapped: onTeeTimeClick)
        }
    }
}

#Preview("Content") {
    TeeTimeCaddieTheme {
        TeeTimesListContent(uiState: .Content(TeeTimeKt.previewTeeTimeList), onTeeTimeClick: { _ in })
    }
}

#Preview("Loading") {
    TeeTimeCaddieTheme {
        TeeTimesListContent(uiState: .Loading, onTeeTimeClick: { _ in })
    }
}

#Preview("Empty") {
    TeeTimeCaddieTheme {
        TeeTimesListContent(uiState: .Empty, onTeeTimeClick: { _ in })
    }
}
