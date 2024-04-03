//
//  DateExtensions.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 3/24/24.
//

import Foundation

extension Date {
    
    static var today: Date {
        return Calendar.current.startOfDay(for: Date())
    }
    
    func add(_ value: Int, interval: Calendar.Component) -> Date {
        return Calendar.current.date(byAdding: interval, value: value, to: self) ?? self.advanced(by: .zero)
    }
}
