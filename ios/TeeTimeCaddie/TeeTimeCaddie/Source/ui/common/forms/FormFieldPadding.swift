import SwiftUI

struct FormFieldPadding: ViewModifier {
    func body(content: Content) -> some View {
        content
            .padding(12)
    }
}

extension View {
    func formFieldPadding() -> some View {
        modifier(FormFieldPadding())
    }
}
