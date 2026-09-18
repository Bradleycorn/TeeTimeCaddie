import SwiftUI

/// Defines the various sources that provide images for the application,
/// including SF Symbols, ImageResources (asset catalog), etc.
enum ImageSource {
    /// An image defined by an ``SfSymbol``.
    case symbol(SfSymbol)
    
    /// An image defined by an `ImageResource` (i.e. an image bundled in an asset catalog).
    case asset(ImageResource)
}

extension Image {
    /// Creates an Image view from an SF Symbol
    /// - Parameters:
    ///   - sfSymbol: A ``SfSymbol`` that specifies the image to load.
    init (_ sfSymbol: SfSymbol) {
        self.init(systemName: sfSymbol.rawValue)
    }
    
    /// Create an Image view from an ImageSource
    /// - Parameters:
    ///   - source: An ``ImageSource`` that defines the image to load.
    init(_ source: ImageSource) {
        switch source {
        case .symbol(let symbol):
            self.init(symbol)
        case .asset(let resource):
            self.init(resource)
        }
    }
}
