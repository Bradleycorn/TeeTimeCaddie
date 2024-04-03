import Foundation
import SwiftUI

public struct ColorPreview: View {
    
    let color: Color
    let name: String
    
    var textColor: Color {
        (color.luminance() < 0.5) ? .white : .black
    }

    public init(_ color: Color, name: String = "") {
        self.color = color
        self.name = name
    }
    
    public var body: some View {
        ZStack(alignment: .topLeading) {
            color
            Text(name)
                .foregroundStyle(textColor.opacity(0.8))
                .font(.system(size: 10))
                .padding(4)
        }.size(50)
    }
}

