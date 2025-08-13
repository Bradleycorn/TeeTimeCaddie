//
//  AppTheme.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 3/14/24.
//

import Foundation
import SwiftUI


public class AppTheme: ObservableObject {
    public let colorScheme: Colors
    public let typography: Typography
    public let shapes: Shapes
    
    init(_ colors: Colors, _ typography: Typography, _ shapes: Shapes) {
        self.colorScheme = colors
        self.typography = typography
        self.shapes = shapes
    }
    
}
