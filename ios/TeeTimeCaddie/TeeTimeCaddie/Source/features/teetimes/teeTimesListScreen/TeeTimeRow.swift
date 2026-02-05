//
//  TeeTimeRow.swift
//  TeeTimeCaddie
//
//  Created by Bradley Ball on 1/13/26.
//

import SwiftUI
import TeeTimeCaddieKit

struct TeeTimeRow: View {
    let teeTime: TeeTime

    var body: some View {
        HStack(alignment: .center, spacing: 16) {
            // Date section (left)
            VStack(alignment: .center, spacing: 4) {
                Text(dayOfWeek)
                    .font(.caption)
                    .foregroundColor(.secondary)

                Text(formattedDate)
                    .font(.headline)
                    .multilineTextAlignment(.center)
            }
            .frame(width: 64)

            // Content section (middle)
            VStack(alignment: .leading, spacing: 4) {
                Text(teeTime.course)
                    .font(.headline)

                HStack(spacing: 4) {
                    Image(.icon)
                        .resizable()
                        .frame(width: 16, height: 16)
                        .foregroundColor(.secondary)

                    // Uses the shared formattedTimes property from KMP
                    Text(teeTime.formattedTimes)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
            }

            Spacer()

            // Total player count (right)
            HStack(spacing: 4) {
                Image(.teeEmpty)
                    .resizable()
                    .frame(width: 20, height: 20)
                    .foregroundColor(.accentColor)

                Text("\(teeTime.totalPlayers)")
                    .font(.headline)
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(8)
        .shadow(color: Color.black.opacity(0.1), radius: 2, x: 0, y: 1)
    }

    private var dayOfWeek: String {
        let calendar = Calendar.current
        let date = teeTime.date.toDate()
        let formatter = DateFormatter()
        formatter.dateFormat = "EEE"
        return formatter.string(from: date)
    }

    private var formattedDate: String {
        let monthName = teeTime.date.month.name.prefix(3)
        return "\(monthName) \(teeTime.date.day)"
    }

}

#Preview {
    TeeTimeRow(teeTime: TeeTimeKt.previewTeeTime)
        .padding()
}
