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
    
    @StateObject
    private var viewModel = TeeTimesListScreenViewModel()

    @State var showAddTeeTimeSheet: Bool = false
        
    var body: some View {
        TeeTimesListContent(uiState: viewModel.uiState)
            .task { await viewModel.loadTeetimes() }
            .toolbar {
                Button(action: { showAddTeeTimeSheet = true }) {
                    Image(.Icons.calendarAdd)
                }
                .enabled(viewModel.addButtonEnabled)
            }
            .sheet(isPresented: $showAddTeeTimeSheet){
                TeeTimeEntryScreen(onTeeTimeCreated: { showAddTeeTimeSheet = false })
                    .presentationDetents([.fraction(0.4)])
                    .presentationDragIndicator(.visible)
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
                    .navigationTitle("Tee Times")
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
