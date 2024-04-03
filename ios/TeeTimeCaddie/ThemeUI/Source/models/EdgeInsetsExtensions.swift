//
//  EdgeInsetsExtensions.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 3/11/24.
//

import Foundation
import SwiftUI

extension EdgeInsets {
    public init(horizontal: CGFloat = 0, vertical: CGFloat = 0) {
        self.init(top: vertical, leading: horizontal, bottom: vertical, trailing: horizontal)
    }
}
