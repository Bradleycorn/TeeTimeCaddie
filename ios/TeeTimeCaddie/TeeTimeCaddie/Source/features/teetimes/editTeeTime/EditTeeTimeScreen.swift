//
//  EditTeeTimeScreen.swift
//  TeeTimeCaddie
//

import SwiftUI
import TeeTimeCaddieKit

struct EditTeeTimeScreen: View {
    let teeTimeId: String
    let onBack: () -> Void
    let onTeeTimeUpdated: () -> Void

    @State
    private var viewModel: EditTeeTimeViewModel?

    @State
    private var showTimePicker: Bool = false

    @State
    private var selectedTimeForPicker: Date = Calendar.current.date(
        bySettingHour: 9,
        minute: 0,
        second: 0,
        of: Date()
    ) ?? Date()

    var body: some View {
        Screen(AnalyticsScreen.EditTeeTime(viewName: self.viewName)) {
            if let vm = viewModel {
                EditTeeTimeContent(
                    viewModel: vm,
                    showTimePicker: $showTimePicker,
                    selectedTimeForPicker: $selectedTimeForPicker
                )
            } else {
                ContentLoadingIndicator()
            }
        }
        .onAppear {
            if viewModel == nil {
                viewModel = EditTeeTimeViewModel(teeTimeId: teeTimeId)
            }
        }
        .sheet(isPresented: $showTimePicker) {
            TimePickerSheet(
                selectedTime: $selectedTimeForPicker,
                onCancel: { showTimePicker = false },
                onConfirm: {
                    viewModel?.addTimeSlot(time: selectedTimeForPicker)
                    showTimePicker = false
                }
            )
        }
        .onChange(of: viewModel?.saveSuccess ?? false) { _, newValue in
            if newValue {
                onTeeTimeUpdated()
            }
        }
    }
}

// MARK: - Content View

fileprivate struct EditTeeTimeContent: View {
    @Bindable var viewModel: EditTeeTimeViewModel
    @Binding var showTimePicker: Bool
    @Binding var selectedTimeForPicker: Date

    private var canSave: Bool {
        viewModel.hasChanges &&
        !viewModel.courseName.isEmpty &&
        !viewModel.timeSlots.isEmpty
    }

    var body: some View {
        if viewModel.isLoading {
            ContentLoadingIndicator()
        } else if viewModel.loadError {
            VStack {
                Text("Failed to load tee time")
                    .foregroundColor(.secondary)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            ScrollView {
                VStack(spacing: 16) {
                    // Course Name TextField
                    TextField(
                        TTR.strings().field_label_course_name.desc().localized(),
                        text: Binding(
                            get: { viewModel.courseName },
                            set: { viewModel.updateCourseName($0) }
                        )
                    )
                    .textFieldStyle(.roundedBorder)
                    .padding(.horizontal)

                    // Date Picker
                    DatePicker(
                        TTR.strings().field_label_Date.desc().localized(),
                        selection: Binding(
                            get: { viewModel.date },
                            set: { viewModel.updateDate($0) }
                        ),
                        displayedComponents: .date
                    )
                    .padding(.horizontal)

                    // Tee Times Section
                    TeeTimesSection(
                        timeSlots: viewModel.timeSlots,
                        onAddTimeClick: {
                            viewModel.onAddTimeClick()
                            showTimePicker = true
                        },
                        onUpdatePlayerCount: viewModel.updatePlayerCount,
                        onRemoveTimeSlot: viewModel.removeTimeSlot
                    )
                    .padding(.horizontal)

                    Spacer()
                        .frame(height: 32)

                    // Save Button
                    LoadingButton(
                        TTR.strings().button_save.desc().localized(),
                        isLoading: viewModel.showLoadingProgress,
                        action: viewModel.saveTeeTime
                    )
                    .buttonStyle(.Filled)
                    .disabled(!canSave)
                }
                .padding(.vertical)
            }
        }
    }
}

// MARK: - Previews

#Preview("Loading") {
    TeeTimeCaddieTheme {
        NavigationStack {
            EditTeeTimeScreen(
                teeTimeId: "preview",
                onBack: {},
                onTeeTimeUpdated: {}
            )
            .navigationTitle(TTR.strings().edit_tee_time.desc().localized())
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
