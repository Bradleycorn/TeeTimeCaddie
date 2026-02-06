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
    private var viewModel: EditTeeTimeViewModel

    @State
    private var showTimePicker: Bool = false

    @State
    private var selectedTimeForPicker: Date = Calendar.current.date(
        bySettingHour: 9,
        minute: 0,
        second: 0,
        of: Date()
    ) ?? Date()

    init(teeTimeId: String, onBack: @escaping () -> Void, onTeeTimeUpdated: @escaping () -> Void) {
        self.teeTimeId = teeTimeId
        self.onBack = onBack
        self.onTeeTimeUpdated = onTeeTimeUpdated
        self._viewModel = State(initialValue: EditTeeTimeViewModel(teeTimeId: teeTimeId))
    }

    var body: some View {
        Screen(AnalyticsScreen.EditTeeTime(viewName: self.viewName)) {
            if viewModel.isLoading {
                ContentLoadingIndicator()
            } else {
                EditTeeTimeContent(
                    courseName: $viewModel.courseName,
                    selectedDate: $viewModel.selectedDate,
                    timeSlots: viewModel.timeSlots,
                    isLoading: viewModel.showSavingProgress,
                    hasChanges: viewModel.hasChanges,
                    onAddTimeClick: {
                        viewModel.onAddTimeClick()
                        showTimePicker = true
                    },
                    onUpdatePlayerCount: viewModel.updatePlayerCount,
                    onRemoveTimeSlot: viewModel.removeTimeSlot,
                    onSave: viewModel.saveTeeTime
                )
            }
        }
        .task { await viewModel.loadTeeTime() }
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
                onTeeTimeUpdated()
            }
        }
    }
}

// MARK: - Content View

fileprivate struct EditTeeTimeContent: View {
    @Binding var courseName: String
    @Binding var selectedDate: Date
    let timeSlots: [TeeTimeSlot]
    let isLoading: Bool
    let hasChanges: Bool
    let onAddTimeClick: () -> Void
    let onUpdatePlayerCount: (LocalTime, Int) -> Void
    let onRemoveTimeSlot: (LocalTime) -> Void
    let onSave: () -> Void

    private var canSave: Bool {
        !courseName.isEmpty && !timeSlots.isEmpty && hasChanges
    }

    var body: some View {
        TeeTimeFormContent(
            courseName: $courseName,
            selectedDate: $selectedDate,
            timeSlots: timeSlots,
            isLoading: isLoading,
            buttonText: GR.strings().save.desc().localized(),
            canSave: canSave,
            onAddTimeClick: onAddTimeClick,
            onUpdatePlayerCount: onUpdatePlayerCount,
            onRemoveTimeSlot: onRemoveTimeSlot,
            onSave: onSave
        )
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
            hasChanges: true,
            onAddTimeClick: {},
            onUpdatePlayerCount: { _, _ in },
            onRemoveTimeSlot: { _ in },
            onSave: {}
        )
    }
}
