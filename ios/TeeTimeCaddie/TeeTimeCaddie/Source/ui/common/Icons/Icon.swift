import SwiftUI


fileprivate let DEFAULT_ICON_SIZE: CGFloat = 24
fileprivate let MIN_ICON_SIZE: CGFloat = 24


/// An Image view with an equal width and height that scales
/// with dynamic type settings.
///
/// By default, an icon will have a width and height of 24 points
/// and will scale relative to the `.body` TextStyle.
///
/// If you pass in your own size value, it is your responsibility
/// to handle scaling the size value based on current dynamic type
/// settings. Consider passing in a value that is managed as a `@ScaledMetric`.
///
/// Although the rendered image can be scaled with dynamic type settings,
/// it has a minimium size of 24 points, and will never be displayed at a size less than that.
struct Icon: View {
    
    /// The image to render
    private let source: ImageSource
    
    /// A default size to use for the image if one is not
    /// passed in by the caller. This size will be scaled
    /// relative to the `.body` TextStyle based on the
    /// user's current dynamic type settings.
    @ScaledMetric(relativeTo: .body)
    private var defaultSize: CGFloat = DEFAULT_ICON_SIZE

    /// An optional custom size passed in by the caller.
    /// If this value is set, it will be used as the size when rendering the image.
    private var size: CGFloat?

    /// A computed property that determines the final rendered size of the
    /// image. It enforces a minimum size value to render an image that is
    /// scaled based on dynamic type settings.
    private var imageSize: CGFloat {
        let s = size ?? defaultSize
        return s < MIN_ICON_SIZE ? MIN_ICON_SIZE : s
    }
    
    /// Show an Icon from an ``ImageSource``.
    /// - Parameters:
    ///   - source: An ``ImageSource`` to render as an icon image.
    ///   - size: An optional `CGFLOAT` that specifies the size at which the icon should be rendered.
    init(_ source: ImageSource, size: CGFloat? = nil) {
        self.source = source
        self.size = size
    }
    
    /// Show an Icon from an ``SfSymbol``.
    /// - Parameters:
    ///   - source: An ``SfSymbol`` to render as an icon image.
    ///   - size: An optional `CGFLOAT` that specifies the size at which the icon should be rendered.
    init(_ symbol: SfSymbol, size: CGFloat? = nil) {
        
        self.init(.symbol(symbol), size: size)
    }

    /// Show an Icon from an ``ImageResource`` defined in the bundled assets catalog.
    /// - Parameters:
    ///   - source: An ``ImageResource`` to render as an icon image.
    ///   - size: An optional `CGFLOAT` that specifies the size at which the icon should be rendered.
    init(_ imageResource: ImageResource, size: CGFloat? = nil) {
        self.init(.asset(imageResource), size: size)
    }
    
    var body: some View {
        Image(source)
            .resizable()
            .aspectRatio(contentMode: .fit)
            .size(imageSize)
    }
}



#Preview {
    
    @Previewable
    @ScaledMetric(relativeTo: .body)
    var iconSize: CGFloat = 64
    
    
    VStack {
        
        Text("Default (dynamic) size")
        Icon(.eye)
        Spacer().frame(height: 64)

        Text("Custom fixed size")
        Icon(.eye, size: 64)
        Spacer().frame(height: 64)

        
        Text("Custom dynamic size")
        Icon(.eye, size: iconSize)
        Spacer().frame(height: 64)

    }
}
