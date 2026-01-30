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
                onAddTimeClick: { showTimePicker = true },
                onUpdatePlayerCount: viewModel.updatePlayerCount,
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
    let onSave: () -> Void

    private var canSave: Bool {
        !courseName.isEmpty && !timeSlots.isEmpty
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
                    onUpdatePlayerCount: onUpdatePlayerCount
                )
                .padding(.horizontal)

                Spacer()
                    .frame(height: 32)

                // Save Button
                LoadingButton(TTR.strings().button_create.desc().localized(), isLoading: isLoading, action: onSave)
                    .buttonStyle(.Filled)
                    .disabled(!canSave)
            }
            .padding(.vertical)
        }
    }
}

// MARK: - Tee Times Section

fileprivate struct TeeTimesSection: View {
    let timeSlots: [TeeTimeSlot]
    let onAddTimeClick: () -> Void
    let onUpdatePlayerCount: (LocalTime, Int) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Section Header
            Text(TTR.strings().tee_times_section_header.desc().localized())
                .font(.headline)

            // List of time slots
            ForEach(timeSlots, id: \.time) { slot in
                TimeSlotRow(
                    slot: slot,
                    onUpdatePlayerCount: { players in
                        onUpdatePlayerCount(slot.time, players)
                    }
                )
                Divider()
            }

            // Add Time Button
            Button(action: onAddTimeClick) {
                HStack {
                    Image(systemName: "plus")
                    Text(TTR.strings().add_time_button.desc().localized())
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
        }
    }
}

// MARK: - Time Slot Row

fileprivate struct TimeSlotRow: View {
    let slot: TeeTimeSlot
    let onUpdatePlayerCount: (Int) -> Void

    private var formattedTime: String {
        let hour = slot.time.hour
        let minute = slot.time.minute
        let displayHour = hour == 0 || hour == 12 ? 12 : hour % 12
        let amPm = hour < 12 ? "AM" : "PM"
        return String(format: "%d:%02d %@", displayHour, minute, amPm)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            // Time display
            Text(formattedTime)
                .font(.headline)

            // Player count slider
            HStack {
                Text(TTR.strings().players_label.desc().localized())
                    .font(.body)

                Slider(
                    value: Binding(
                        get: { Double(slot.numberOfPlayers) },
                        set: { onUpdatePlayerCount(Int($0)) }
                    ),
                    in: 1...4,
                    step: 1
                )

                Text("\(slot.numberOfPlayers)")
                    .font(.headline)
                    .frame(width: 30)
            }
        }
        .padding(.vertical, 4)
    }
}

// MARK: - Time Picker Sheet

fileprivate struct TimePickerSheet: View {
    @Binding var selectedTime: Date
    let onCancel: () -> Void
    let onConfirm: () -> Void

    var body: some View {
        NavigationStack {
            DatePicker(
                "",
                selection: $selectedTime,
                displayedComponents: .hourAndMinute
            )
            .datePickerStyle(.wheel)
            .labelsHidden()
            .navigationTitle(TTR.strings().field_Label_Time.desc().localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(GR.strings().cancel.desc().localized()) {
                        onCancel()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(GR.strings().ok.desc().localized()) {
                        onConfirm()
                    }
                }
            }
        }
        .presentationDetents([.medium])
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
            onSave: {}
        )
    }
}
