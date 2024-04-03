import Foundation
import SwiftUI

public class ButtonDefaults {
    
    public static var padding: EdgeInsets {
        EdgeInsets(horizontal: 16, vertical: 8)
    }
    
    public static func colors(from colorScheme: Colors) -> ButtonColors {
        return colorScheme.deaultButtonColors
    }
    
    public static func outlinedColors(from colorScheme: Colors) -> ButtonColors {
        return colorScheme.outlinedButtonColors
    }

    public static let outlinedButtonBorderWidth: CGFloat = 1
    
    public static let shape: some Shape = .capsule
}
