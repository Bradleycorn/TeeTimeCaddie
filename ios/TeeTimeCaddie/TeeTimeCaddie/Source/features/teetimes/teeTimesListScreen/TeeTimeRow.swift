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

                    Text(formattedTimes)
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

    private var formattedTimes: String {
        // Sort times and format each one
        let sortedSlots = teeTime.times.sorted { compareLocalTime($0.time, $1.time) }
        return sortedSlots.map { formatTime($0.time) }.joined(separator: ", ")
    }

    private func formatTime(_ time: LocalTime) -> String {
        let hour = time.hour == 0 || time.hour == 12 ? 12 : Int(time.hour) % 12
        let minute = String(format: "%02d", time.minute)
        let amPm = time.hour < 12 ? "AM" : "PM"
        return "\(hour):\(minute) \(amPm)"
    }
}

// Extension to convert Kotlin LocalDate to Swift Date
private extension LocalDate {
    func toDate() -> Date {
        let calendar = Calendar.current
        let components = DateComponents(
            year: Int(self.year),
            month: Int(self.month.number),
            day: Int(self.day)
        )
        return calendar.date(from: components) ?? Date()
    }
}

#Preview {
    TeeTimeRow(teeTime: TeeTimeKt.previewTeeTime)
        .padding()
}
