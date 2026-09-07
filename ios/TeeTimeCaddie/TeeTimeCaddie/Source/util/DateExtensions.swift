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
}


// MARK: - LocalDate conversion

// The shared KMP SDK speaks `kotlinx.datetime.LocalDate` (exposed by SKIE as plain `LocalDate`),
// while SwiftUI speaks `Date`. These two bridge that boundary — ViewModels convert here so the
// views themselves can stay `Date`-only. Both drop the time of day: a `LocalDate` is a calendar
// day, so the `Date` side is always local midnight.

extension LocalDate {
    /// Converts a Kotlin LocalDate to a Swift Date, at midnight in the current calendar.
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

extension Date {
    /// Converts a Swift Date to a Kotlin LocalDate, taking the calendar day in the current timezone.
    func toLocalDate() -> LocalDate {
        let components = Calendar.current.dateComponents([.year, .month, .day], from: self)
        return LocalDate(
            year: Int32(components.year ?? 1970),
            month: Int32(components.month ?? 1),
            day: Int32(components.day ?? 1)
        )
    }
}

// MARK: - LocalTime conversion

// The twins of the `LocalDate` pair above, for `kotlinx.datetime.LocalTime` (also exposed by SKIE
// under its plain Kotlin name). Same split: views stay `Date`-only and ViewModels convert here.
// Both drop the calendar day — a `LocalTime` is a time of day, so the `Date` side carries an
// arbitrary date and only its hour and minute are meaningful.

extension LocalTime {
    /// Converts a Kotlin LocalTime to a Swift Date, on today's date in the current calendar.
    ///
    /// The calendar day is arbitrary — a LocalTime has none. Only the time of day is meaningful, and
    /// that is all `DatePicker(displayedComponents: .hourAndMinute)` reads.
    func toDate() -> Date {
        let calendar = Calendar.current
        return calendar.date(
            bySettingHour: Int(self.hour),
            minute: Int(self.minute),
            second: 0,
            of: .today
        ) ?? .today
    }
}

extension Date {
    /// Converts a Swift Date to a Kotlin LocalTime, taking the time of day in the current calendar.
    func toLocalTime() -> LocalTime {
        let components = Calendar.current.dateComponents([.hour, .minute], from: self)
        return LocalTime(
            hour: Int32(components.hour ?? 0),
            minute: Int32(components.minute ?? 0),
            second: 0,
            nanosecond: 0
        )
    }
}
