//
//  EditTeeTimeScreen.swift
//  TeeTimeCaddie
//
//  Created by Claude on 2/6/26.
//

import SwiftUI
import TeeTimeCaddieKit

struct EditTeeTimeScreen: View {
    let teeTime: TeeTime
    let onBack: () -> Void
    let onTeeTimeSaved: () -> Void

    @State
    private var viewModel = EditTeeTimeViewModel()

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
            EditTeeTimeContent(
                courseName: Binding(
                    get: { viewModel.courseName },
                    set: { viewModel.courseName = $0 }
                ),
                selectedDate: Binding(
                    get: { viewModel.selectedDate },
                    set: { viewModel.selectedDate = $0 }
                ),
                timeSlots: viewModel.timeSlots,
                isLoading: viewModel.showLoadingProgress,
                canSave: canSave,
                onAddTimeClick: {
                    viewModel.onAddTimeClick()
                    showTimePicker = true
                },
                onUpdatePlayerCount: viewModel.updatePlayerCount,
                onRemoveTimeSlot: viewModel.removeTimeSlot,
                onSave: viewModel.saveTeeTime
            )
        }
        .onAppear {
            viewModel.initialize(teeTime: teeTime)
        }
        .sheet(isPresented: $showTimePicker) {
            TimePickerSheet(
                selectedTime: $selectedTimeForPicker,
                onCancel: { showTimePicker = false },
                onConfirm: {
                    viewModel.addTimeSlot(time: selectedTimeForPicker)
                    showTimePicker = false
                }
            )
        }
        .onChange(of: viewModel.saveSuccess) { _, newValue in
            if newValue {
                onTeeTimeSaved()
            }
        }
    }

    private var canSave: Bool {
        !viewModel.courseName.isEmpty &&
        !viewModel.timeSlots.isEmpty &&
        viewModel.hasChanges
    }
}

// MARK: - Content View

fileprivate struct EditTeeTimeContent: View {
    @Binding var courseName: String
    @Binding var selectedDate: Date
    let timeSlots: [TeeTimeSlot]
    let isLoading: Bool
    let canSave: Bool
    let onAddTimeClick: () -> Void
    let onUpdatePlayerCount: (LocalTime, Int) -> Void
    let onRemoveTimeSlot: (LocalTime) -> Void
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

                // Tee Times Section
                TeeTimesSection(
                    timeSlots: timeSlots,
                    onAddTimeClick: onAddTimeClick,
                    onUpdatePlayerCount: onUpdatePlayerCount,
                    onRemoveTimeSlot: onRemoveTimeSlot
                )
                .padding(.horizontal)

                Spacer()
                    .frame(height: 32)

                // Save Button
                LoadingButton(GR.strings().save.desc().localized(), isLoading: isLoading, action: onSave)
                    .buttonStyle(.Filled)
                    .disabled(!canSave)
            }
            .padding(.vertical)
        }
    }
}

// MARK: - Previews

#Preview("Edit Tee Time") {
    TeeTimeCaddieTheme {
        NavigationStack {
            EditTeeTimeContentPreviewWrapper()
                .navigationTitle(TTR.strings().edit_tee_time.desc().localized())
                .navigationBarTitleDisplayMode(.inline)
        }
    }
}

fileprivate struct EditTeeTimeContentPreviewWrapper: View {
    @State var courseName: String = "Persimmon Ridge"
    @State var selectedDate: Date = Date()

    var body: some View {
        EditTeeTimeContent(
            courseName: $courseName,
            selectedDate: $selectedDate,
            timeSlots: TeeTimeKt.previewTeeTimeSlotList,
            isLoading: false,
            canSave: true,
            onAddTimeClick: {},
            onUpdatePlayerCount: { _, _ in },
            onRemoveTimeSlot: { _ in },
            onSave: {}
        )
    }
}
