import Foundation
import SwiftUI

import SwiftUI

public struct Size: ViewModifier {
    let size: CGFloat?
    let alignment: Alignment
    
    public init(_ size: CGFloat?, alignment: Alignment = .center) {
        self.size = size
        self.alignment = alignment
    }
    
    public func body(content: Content) -> some View {
        content.frame(width: size, height: size, alignment: alignment)
    }
}

extension View {
    public func size(_ size: CGFloat, alignment: Alignment = .center) -> some View {
        modifier(Size(size, alignment: alignment))
    }
}

