import SwiftUI

extension View {
    var viewName: String {
        String(describing: Self.self)
    }
}

extension Label where Title == Text, Icon == Image {
    /// Creates a Label with a Text view and an image from an ImageSource
    /// - Parameters:
    ///   - title: The Text view to display
    ///   - source: An ImageSource that defines the image to load
    init(_ title: String, icon: ImageResource) {
        self.init {
            Text(title)
        } icon: {
            Image(icon)
        }
    }
}
