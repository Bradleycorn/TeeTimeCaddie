//
//  TeeTimeFormComponents.swift
//  TeeTimeCaddie
//
//  Shared components for Add and Edit Tee Time screens.
//

import SwiftUI
import TeeTimeCaddieKit

// MARK: - Tee Times Section

/// A section displaying tee time slots with the ability to add, update, and remove slots.
///
/// This view is shared between the Add and Edit tee time screens.
struct TeeTimesSection: View {
    let timeSlots: [TeeTimeSlot]
    let onAddTimeClick: () -> Void
    let onUpdatePlayerCount: (LocalTime, Int) -> Void
    let onRemoveTimeSlot: (LocalTime) -> Void

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
                    },
                    onRemove: {
                        onRemoveTimeSlot(slot.time)
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

/// A row displaying a single time slot with player count slider and remove button.
///
/// This view is shared between the Add and Edit tee time screens.
struct TimeSlotRow: View {
    let slot: TeeTimeSlot
    let onUpdatePlayerCount: (Int) -> Void
    let onRemove: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            // Time display with trash icon
            HStack {
                Text(slot.time.formattedTime)
                    .font(.headline)

                Spacer()

                Button(action: onRemove) {
                    Image("icons/trash")
                        .foregroundColor(.red)
                }
                .buttonStyle(.plain)
                .accessibilityLabel(GR.strings().delete.desc().localized())
            }

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

/// A sheet for selecting a time for a tee time slot.
///
/// This view is shared between the Add and Edit tee time screens.
struct TimePickerSheet: View {
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

// MARK: - Tee Time Form Content

/// A shared form content view for Add and Edit tee time screens.
///
/// This view encapsulates the entire form layout including:
/// - Course name text field
/// - Date picker
/// - Tee times section with time slots
/// - Save/Create button
struct TeeTimeFormContent: View {
    @Binding var courseName: String
    @Binding var selectedDate: Date
    let timeSlots: [TeeTimeSlot]
    let isLoading: Bool
    let buttonText: String
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

                // Save/Create Button
                LoadingButton(buttonText, isLoading: isLoading, action: onSave)
                    .buttonStyle(.Filled)
                    .disabled(!canSave)
            }
            .padding(.vertical)
        }
    }
}

// MARK: - Previews

#Preview("Tee Times Section - Empty") {
    TeeTimeCaddieTheme {
        TeeTimesSection(
            timeSlots: [],
            onAddTimeClick: {},
            onUpdatePlayerCount: { _, _ in },
            onRemoveTimeSlot: { _ in }
        )
        .padding()
    }
}

#Preview("Tee Times Section - With Slots") {
    TeeTimeCaddieTheme {
        TeeTimesSection(
            timeSlots: TeeTimeKt.previewTeeTimeSlotList,
            onAddTimeClick: {},
            onUpdatePlayerCount: { _, _ in },
            onRemoveTimeSlot: { _ in }
        )
        .padding()
    }
}

#Preview("Tee Time Form Content - Empty") {
    TeeTimeCaddieTheme {
        TeeTimeFormContentPreviewWrapper(courseName: "", timeSlots: [])
    }
}

#Preview("Tee Time Form Content - With Data") {
    TeeTimeCaddieTheme {
        TeeTimeFormContentPreviewWrapper(
            courseName: "Persimmon Ridge",
            timeSlots: TeeTimeKt.previewTeeTimeSlotList
        )
    }
}

fileprivate struct TeeTimeFormContentPreviewWrapper: View {
    @State var courseName: String
    @State var selectedDate: Date = Date()
    let timeSlots: [TeeTimeSlot]

    var body: some View {
        TeeTimeFormContent(
            courseName: $courseName,
            selectedDate: $selectedDate,
            timeSlots: timeSlots,
            isLoading: false,
            buttonText: "Create",
            canSave: !courseName.isEmpty && !timeSlots.isEmpty,
            onAddTimeClick: {},
            onUpdatePlayerCount: { _, _ in },
            onRemoveTimeSlot: { _ in },
            onSave: {}
        )
    }
}
