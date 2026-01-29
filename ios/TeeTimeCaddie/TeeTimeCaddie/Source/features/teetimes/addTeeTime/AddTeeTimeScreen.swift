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

    @State
    var courseName: String = ""

    @State
    var selectedDate: Date = Date()

    @State
    var selectedTime: Date = Date()

    @State
    var players: Int = 4

    var body: some View {
        Screen(AnalyticsScreen.AddTeeTime(viewName: self.viewName)) {
            AddTeeTimeContent(
                courseName: $courseName,
                selectedDate: $selectedDate,
                selectedTime: $selectedTime,
                players: $players,
                isLoading: viewModel.showLoadingProgress,
                onSave: {
                    viewModel.saveTeeTime(
                        courseName: courseName,
                        selectedDate: selectedDate,
                        selectedTime: selectedTime,
                        numberOfPlayers: players
                    )
                }
            )
        }
        .onChange(of: viewModel.saveSuccess) { _, newValue in
            if newValue {
                onTeeTimeCreated()
            }
        }
    }
}

fileprivate struct AddTeeTimeContent: View {
    @Binding var courseName: String
    @Binding var selectedDate: Date
    @Binding var selectedTime: Date
    @Binding var players: Int
    let isLoading: Bool
    let onSave: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Course Name TextField
                TextField(TTR.strings().field_label_course_name.desc().localized(), text: $courseName)
                    .textFieldStyle(.roundedBorder)
                    .padding(.horizontal)

                // Date Picker
                DatePicker(
                    TTR.strings().field_label_Date.desc().localized(),
                    selection: $selectedDate,
                    displayedComponents: .date
                )
                .padding(.horizontal)

                // Time Picker
                DatePicker(
                    TTR.strings().field_Label_Time.desc().localized(),
                    selection: $selectedTime,
                    displayedComponents: .hourAndMinute
                )
                .padding(.horizontal)

                // Number of Players Slider
                VStack(alignment: .leading, spacing: 8) {
                    Text(TTR.strings().field_Label_Players.desc().localized())
                        .font(.body)
                        .padding(.horizontal)

                    HStack {
                        Slider(
                            value: Binding(
                                get: { Double(players) },
                                set: { players = Int($0) }
                            ),
                            in: 1...4,
                            step: 1
                        )
                        .padding(.leading)

                        Text("\(players)")
                            .font(.headline)
                            .frame(width: 40)
                            .padding(.trailing)
                    }
                }

                Spacer()
                    .frame(height: 32)

                // Save Button
                LoadingButton(TTR.strings().button_create.desc().localized(), isLoading: isLoading, action: onSave)
                    .buttonStyle(.Filled)
            }
            .padding(.vertical)
        }
    }
}

#Preview {
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
