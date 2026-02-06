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
    private var showTimePicker: Bool = false

    @State
    private var selectedTimeForPicker: Date = Calendar.current.date(
        bySettingHour: 9,
        minute: 0,
        second: 0,
        of: Date()
    ) ?? Date()

    var body: some View {
        Screen(AnalyticsScreen.AddTeeTime(viewName: self.viewName)) {
            AddTeeTimeContent(
                courseName: $courseName,
                selectedDate: $selectedDate,
                timeSlots: viewModel.timeSlots,
                isLoading: viewModel.showLoadingProgress,
                onAddTimeClick: {
                    viewModel.onAddTimeClick()
                    showTimePicker = true
                },
                onUpdatePlayerCount: viewModel.updatePlayerCount,
                onRemoveTimeSlot: viewModel.removeTimeSlot,
                onSave: {
                    viewModel.saveTeeTime(
                        courseName: courseName,
                        selectedDate: selectedDate
                    )
                }
            )
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
                onTeeTimeCreated()
            }
        }
    }
}

// MARK: - Content View

fileprivate struct AddTeeTimeContent: View {
    @Binding var courseName: String
    @Binding var selectedDate: Date
    let timeSlots: [TeeTimeSlot]
    let isLoading: Bool
    let onAddTimeClick: () -> Void
    let onUpdatePlayerCount: (LocalTime, Int) -> Void
    let onRemoveTimeSlot: (LocalTime) -> Void
    let onSave: () -> Void

    private var canSave: Bool {
        !courseName.isEmpty && !timeSlots.isEmpty
    }

    var body: some View {
        TeeTimeFormContent(
            courseName: $courseName,
            selectedDate: $selectedDate,
            timeSlots: timeSlots,
            isLoading: isLoading,
            buttonText: TTR.strings().button_create.desc().localized(),
            canSave: canSave,
            onAddTimeClick: onAddTimeClick,
            onUpdatePlayerCount: onUpdatePlayerCount,
            onRemoveTimeSlot: onRemoveTimeSlot,
            onSave: onSave
        )
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
        AddTeeTimeContent(
            courseName: $courseName,
            selectedDate: $selectedDate,
            timeSlots: TeeTimeKt.previewTeeTimeSlotList,
            isLoading: false,
            onAddTimeClick: {},
            onUpdatePlayerCount: { _, _ in },
            onRemoveTimeSlot: { _ in },
            onSave: {}
        )
    }
}
