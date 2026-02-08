//
//  TeeTimesSection.swift
//  TeeTimeCaddie
//

import SwiftUI
import TeeTimeCaddieKit

/// A section displaying a list of tee time slots with an "Add Time" button.
///
/// This view is shared between the Add and Edit Tee Time screens.
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
