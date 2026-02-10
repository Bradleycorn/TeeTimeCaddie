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

// MARK: - LocalDate to Date conversion
extension LocalDate {
    /// Converts a Kotlin LocalDate to a Swift Date.
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
