//
//  TimeSlotRow.swift
//  TeeTimeCaddie
//

import SwiftUI
import TeeTimeCaddieKit

/// A row displaying a single tee time slot with time, player count slider, and remove button.
///
/// This view is shared between the Add and Edit Tee Time screens.
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
