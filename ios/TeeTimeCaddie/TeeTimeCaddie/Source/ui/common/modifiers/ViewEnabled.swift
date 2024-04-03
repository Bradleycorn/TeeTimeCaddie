import Foundation
import SwiftUI

fileprivate struct ViewEnabled: ViewModifier {
    private let enabled: Bool
    
    init(_ enabled: Bool) {
        self.enabled = enabled
    }

    func body(content: Content) -> some View {
        content.disabled(!enabled)
    }
}

extension View {
    func enabled(_ enabled: Bool) -> some View {
        self.modifier(ViewEnabled(enabled))
    }
}

