//
//  TeeTimesSection.swift
//  TeeTimeCaddie
//

import SwiftUI
import TeeTimeCaddieKit

/// A section displaying a list of tee time slots with the ability to add, update, and remove times.
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

#Preview("Empty") {
    TeeTimeCaddieTheme {
        TeeTimesSection(
            timeSlots: [],
            onAddTimeClick: { },
            onUpdatePlayerCount: { _, _ in },
            onRemoveTimeSlot: { _ in }
        )
        .padding()
    }
}

#Preview("With Times") {
    TeeTimeCaddieTheme {
        TeeTimesSection(
            timeSlots: TeeTimeKt.previewTeeTimeSlotList,
            onAddTimeClick: { },
            onUpdatePlayerCount: { _, _ in },
            onRemoveTimeSlot: { _ in }
        )
        .padding()
    }
}
