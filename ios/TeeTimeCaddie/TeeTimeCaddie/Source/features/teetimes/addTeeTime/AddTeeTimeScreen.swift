//
//  AddTeeTimeScreen.swift
//  TeeTimeCaddie
//
//  Created by Bradley Ball on 1/10/26.
//

import SwiftUI
import TeeTimeCaddieKit

struct AddTeeTimeScreen: View {
    let onBack: () -> Void
    let onTeeTimeCreated: () -> Void

    @State
    private var viewModel = AddTeeTimeViewModel()

    var body: some View {
        Screen(AnalyticsScreen.AddTeeTime(viewName: self.viewName)) {
            AddTeeTimeContent()
        }
    }
}

// MARK: - Content View

fileprivate struct AddTeeTimeContent: View {

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Text("Add Tee Time Placeholder")
            }
            .padding(.vertical)
        }
    }
}

// MARK: - Previews

#Preview("Empty") {
    TeeTimeCaddieTheme {
        NavigationStack {
            AddTeeTimeScreen(
                onBack: {},
                onTeeTimeCreated: {}
            )
            .navigationTitle(TTR.strings().add_tee_time.desc().localized())
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

#Preview("With Times") {
    TeeTimeCaddieTheme {
        NavigationStack {
            AddTeeTimeContentPreviewWrapper()
                .navigationTitle(TTR.strings().add_tee_time.desc().localized())
                .navigationBarTitleDisplayMode(.inline)
        }
    }
}

fileprivate struct AddTeeTimeContentPreviewWrapper: View {
    @State var courseName: String = "Persimmon Ridge"
    @State var selectedDate: Date = Date()

    var body: some View {
        AddTeeTimeContent()
    }
}
