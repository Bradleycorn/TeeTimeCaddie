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
                if vm.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    EditTeeTimeContent(
                        courseName: Binding(
                            get: { vm.courseName },
                            set: { vm.updateCourseName($0) }
                        ),
                        selectedDate: Binding(
                            get: { vm.selectedDate },
                            set: { vm.updateDate($0) }
                        ),
                        timeSlots: vm.timeSlots,
                        isLoading: vm.showSaveProgress,
                        hasChanges: vm.hasChanges,
                        onAddTimeClick: {
                            vm.onAddTimeClick()
                            showTimePicker = true
                        },
                        onUpdatePlayerCount: vm.updatePlayerCount,
                        onRemoveTimeSlot: vm.removeTimeSlot,
                        onSave: vm.saveTeeTime
                    )
                }
            } else {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
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
        hasChanges && !courseName.isEmpty && !timeSlots.isEmpty
    }

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
                LoadingButton(TTR.strings().button_save.desc().localized(), isLoading: isLoading, action: onSave)
                    .buttonStyle(.Filled)
                    .disabled(!canSave)
            }
            .padding(.vertical)
        }
    }
}

// MARK: - Previews

#Preview("Edit Screen") {
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
