//
//  DateExtensions.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 3/24/24.
//

import Foundation
import TeeTimeCaddieKit

extension Date {

    static var today: Date {
        return Calendar.current.startOfDay(for: Date())
    }

    func add(_ value: Int, interval: Calendar.Component) -> Date {
        return Calendar.current.date(byAdding: interval, value: value, to: self) ?? self.advanced(by: .zero)
    }

    /// Converts a Swift Date to a Kotlin LocalDate.
    func toLocalDate() -> LocalDate {
        let calendar = Calendar.current
        let components = calendar.dateComponents([.year, .month, .day], from: self)
        return LocalDate(
            year: Int32(components.year ?? 2024),
            month: Int32(components.month ?? 1),
            day: Int32(components.day ?? 1)
        )
    }

    /// Converts a Swift Date to a Kotlin LocalTime.
    func toLocalTime() -> LocalTime {
        let calendar = Calendar.current
        let components = calendar.dateComponents([.hour, .minute], from: self)
        return LocalTime(
            hour: Int32(components.hour ?? 0),
            minute: Int32(components.minute ?? 0),
            second: 0,
            nanosecond: 0
        )
    }
}

extension LocalDate {
    /// Converts a Kotlin LocalDate to a Swift Date.
    func toDate() -> Date {
        let calendar = Calendar.current
        let components = DateComponents(
            year: Int(self.year),
            month: Int(self.monthNumber),
            day: Int(self.dayOfMonth)
        )
        return calendar.date(from: components) ?? Date()
    }
}
