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
    let onTeeTimeClick: (String) -> Void

    @State
    private var viewModel = TeeTimesListScreenViewModel()

    var body: some View {
        Screen(AnalyticsScreen.TeeTimeList(viewName: self.viewName)) {
            TeeTimesListContent(
                onTeeTimeClick: { teeTime in onTeeTimeClick(teeTime.id!) }
            )
        }
        .toolbar {
            Button(action: onAddTeeTimeClick) {
                Image(.Icons.calendarAdd)
            }
        }
    }
}

fileprivate struct TeeTimesListContent: View {
    private var onTeeTimeClick: (TeeTime) -> Void

    init(onTeeTimeClick: @escaping (TeeTime) -> Void = {_ in}) {
        self.onTeeTimeClick = onTeeTimeClick
    }

    var body: some View {
        Text("Tee Times List")
    }
}

#Preview("Content") {
    TeeTimeCaddieTheme {
        TeeTimesListContent()
    }
}
