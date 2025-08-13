import SwiftUI

public struct Surface<Content: View>: View {
    
    @EnvironmentObject
    private var theme: AppTheme

    private let color: Color?
    private let contentColor: Color?
    private let alignment: Alignment
    private let consumeEdges: Edge.Set

    let content: () -> Content

    public init(color: Color? = nil, contentColor: Color? = nil, alignment: Alignment = .center, consumeEdges: Edge.Set = [], @ViewBuilder content: @escaping () -> Content) {
        self.color = color
        self.contentColor = contentColor
        self.alignment = alignment
        self.consumeEdges = consumeEdges
        self.content = content
    }
    
    private var background: Color {
        color ?? theme.colorScheme.surface
    }
    
    private var foreground: Color {
        contentColor ?? theme.colorScheme.contentColorFor(background)
    }
    
    public var body: some View {
        ZStack(alignment: alignment) {
            background.ignoresSafeArea(.all, edges: consumeEdges)
            content()
        }
        .foregroundStyle(foreground)
    }
}

struct Surface_Previews: PreviewProvider {

    struct PreviewContent: View {
        @EnvironmentObject var theme: AppTheme

        var body: some View {
            Surface(color: theme.colorScheme.primaryContainer, alignment: .top, consumeEdges: .top) {
                VStack {
                    Text("Surface Content")
                    Button(action: {}) {
                        Text("Button")
                    }
                    .buttonStyle(FilledButtonStyle())
                }
           }
        }
    }
    
    static var previews: some View {
        DefaultTheme {
            PreviewContent()
        }
    }
    
}
