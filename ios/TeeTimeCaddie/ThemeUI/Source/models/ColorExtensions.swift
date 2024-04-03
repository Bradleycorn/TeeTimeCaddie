//
//  ColorExtensions.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 2/24/24.
//

import Foundation
import SwiftUI

extension Color {
    
    /// Create a color from an aRGB hex value.
    ///
    /// This initializer allows you to create a Color instance using an aRGB hex color value, 
    /// For example, 0xff00ff00 represents the color Green.
    ///
    /// - Parameter hexNumber: The 32-bit ARGB color int to create a Color from. For example 0xff009900.
    public init(_ hexNumber: UInt64) {
        let r, g, b, a: CGFloat

        a = CGFloat((hexNumber & 0xff000000) >> 24) / 255
        r = CGFloat((hexNumber & 0x00ff0000) >> 16) / 255
        g = CGFloat((hexNumber & 0x0000ff00) >> 8) / 255
        b = CGFloat(hexNumber & 0x000000ff) / 255
        
        self.init(uiColor: UIColor(red: r, green: g, blue: b, alpha: a))
    }
    
    public init(red: Int, green: Int, blue: Int) {
        
        let max: Double = 255
        
        let r: Double = Double(red) / max
        let g: Double = Double(green) / max
        let b: Double = Double(blue) / max
        
        self.init(red: r, green: g, blue: b)
    }
    
    
    func luminance() -> CGFloat {
        // https://www.w3.org/TR/WCAG20-TECHS/G18.html#G18-tests

        let ciColor = CIColor(color: UIColor(self))

        func adjust(colorComponent: CGFloat) -> CGFloat {
            return (colorComponent < 0.04045) ? (colorComponent / 12.92) : pow((colorComponent + 0.055) / 1.055, 2.4)
        }

        return 0.2126 * adjust(colorComponent: ciColor.red) + 0.7152 * adjust(colorComponent: ciColor.green) + 0.0722 * adjust(colorComponent: ciColor.blue)
    }
    
    static func contrastRatio(between color1: Color, and color2: Color) -> CGFloat {
        // https://www.w3.org/TR/WCAG20-TECHS/G18.html#G18-tests

        let luminance1 = color1.luminance()
        let luminance2 = color2.luminance()

        let luminanceDarker = min(luminance1, luminance2)
        let luminanceLighter = max(luminance1, luminance2)

        return (luminanceLighter + 0.05) / (luminanceDarker + 0.05)
    }

    func contrastRatio(with color: Color) -> CGFloat {
        return Color.contrastRatio(between: self, and: color)
    }
}
