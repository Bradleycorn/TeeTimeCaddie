import SwiftUI

public struct ThemedView<Content: View>: View {
    private let theme: AppTheme
    private let content: () -> Content
    
    public init(colors: Colors, typography: Typography, shapes: Shapes, @ViewBuilder content: @escaping ()->Content) {
        theme = AppTheme(colors, typography, shapes)
        self.content = content
    }

    public var body: some View {
        content()
            .environmentObject(theme)
            .environment(\.font, theme.typography.bodyLarge)
    }
}


struct DefaultTheme<Content: View>: View {
    private let content: () -> Content

    @Environment(\.colorScheme) var colorMode
    
    private var colors: Colors {
        switch colorMode {
            case .dark:
                darkColorScheme()
            default:
                lightColorScheme()
        }
    }
    
    init(
        @ViewBuilder content: @escaping ()->Content) {
            self.content = content
    }
    
    var body: some View {
        ThemedView(colors: colors, typography: Typography(), shapes: Shapes(), content: content)
    }
}
