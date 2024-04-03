//
//  Typography.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 2/26/24.
//

import Foundation
import SwiftUI

public class Typography: ObservableObject {
    @Published public private(set) var displayLarge: Font
    @Published public private(set) var displayMedium: Font
    @Published public private(set) var displaySmall: Font
    @Published public private(set) var headlineLarge: Font
    @Published public private(set) var headlineMedium: Font
    @Published public private(set) var headlineSmall: Font
    @Published public private(set) var titleLarge: Font
    @Published public private(set) var titleMedium: Font
    @Published public private(set) var titleSmall: Font
    @Published public private(set) var bodyLarge: Font
    @Published public private(set) var bodyMedium: Font
    @Published public private(set) var bodySmall: Font
    @Published public private(set) var labelLarge: Font
    @Published public private(set) var labelMedium: Font
    @Published public private(set) var labelSmall: Font

    public init(
        displayLarge: Font   = Font.system(size: 57, weight: .medium),
        displayMedium: Font  = Font.system(size: 45, weight: .medium),
        displaySmall: Font   = Font.system(size: 36, weight: .medium),
        headlineLarge: Font  = Font.system(size: 32, weight: .medium),
        headlineMedium: Font = Font.system(size: 28, weight: .medium),
        headlineSmall: Font  = Font.system(size: 24, weight: .medium),
        titleLarge: Font     = Font.system(size: 22, weight: .medium),
        titleMedium: Font    = Font.system(size: 16, weight: .semibold),
        titleSmall: Font     = Font.system(size: 14, weight: .semibold),
        bodyLarge: Font      = Font.system(size: 16, weight: .medium),
        bodyMedium: Font     = Font.system(size: 14, weight: .medium),
        bodySmall: Font      = Font.system(size: 12, weight: .medium),
        labelLarge: Font     = Font.system(size: 14, weight: .semibold),
        labelMedium: Font    = Font.system(size: 12, weight: .semibold),
        labelSmall: Font     = Font.system(size: 11, weight: .semibold)) {
        
        self.displayLarge = displayLarge
        self.displayMedium = displayMedium
        self.displaySmall = displaySmall
        self.headlineLarge = headlineLarge
        self.headlineMedium = headlineMedium
        self.headlineSmall = headlineSmall
        self.titleLarge = titleLarge
        self.titleMedium = titleMedium
        self.titleSmall = titleSmall
        self.bodyLarge = bodyLarge
        self.bodyMedium = bodyMedium
        self.bodySmall = bodySmall
        self.labelLarge = labelLarge
        self.labelMedium = labelMedium
        self.labelSmall = labelSmall
    }
}
