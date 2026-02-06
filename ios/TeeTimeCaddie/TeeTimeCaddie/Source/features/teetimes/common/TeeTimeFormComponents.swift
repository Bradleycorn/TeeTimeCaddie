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
