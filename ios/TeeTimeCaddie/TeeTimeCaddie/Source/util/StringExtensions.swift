//
//  StringExtensions.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 3/25/24.
//

import Foundation

extension String {
    func insert(_ text: String, at index: Int) -> String {
        var newString = self
        newString.insert(contentsOf: text, at: newString.index(startIndex, offsetBy: index))
        return newString
    }
}
