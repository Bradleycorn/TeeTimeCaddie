import SwiftUI

/// Defines the various sources that provide images for the application,
/// including SF Symbols, ImageResources (asset catalog), etc.
enum ImageSource {
    /// An image defined by an ``SfSymbol``.
    case symbol(SfSymbol)
    
    /// An image defined by an `ImageResource` (i.e. an image bundled in an asset catalog).
    case asset(ImageResource)
}

extension ImageSource {
    /// A copy of this image already sized for a tab bar item.
    ///
    /// `.tabItem` renders through `UITabBarItem` and **discards SwiftUI modifiers on its icon**, so
    /// `.resizable().frame(...)` has no effect there. UIKit scales SF Symbols itself, but a vector
    /// asset draws at its intrinsic size — and the brand mark is 496pt square, so the Games tab's
    /// glyph spilled out of the tab bar.
    ///
    /// Resizing the asset itself is not the fix: the same artwork is the app icon and the auth
    /// screen's brand lockup, both of which want it bigger. The size belongs to this one use.
    @MainActor
    func tabBarImage(size: CGFloat) -> Image {
        switch self {
        case .symbol:
            // UIKit already scales symbols to the tab bar's metrics.
            return Image(self)
        case .asset(let resource):
            let target = CGSize(width: size, height: size)
            let scaled = UIGraphicsImageRenderer(size: target).image { _ in
                UIImage(resource: resource).draw(in: CGRect(origin: .zero, size: target))
            }
            return Image(uiImage: scaled.withRenderingMode(.alwaysTemplate))
        }
    }
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
