import SwiftUI

struct LoadingOverlay: ViewModifier {
    let isLoading: Bool
    let type: DotType
    
    func body(content: Content) -> some View {
        content
            .opacity(isLoading ? 0 : 1)
            .overlay(alignment: .center) {
                if (isLoading) {
                    LoadingIndicator(type)
                } else {
                    EmptyView()
                }
            }
    }
}

extension View {
    func loadingOverlay(type: DotType = .Flashing, isLoading: Bool) -> some View {
        modifier(LoadingOverlay(isLoading: isLoading, type: type))
    }
}


fileprivate struct LoadingOverlayPreviews: View {
    @State private var isLoading = false

    var body: some View {
        VStack {
            Text("Click Me")
                .onTapGesture { isLoading.toggle() }
                .loadingOverlay(isLoading: isLoading)
        }
    }
}

#Preview("Light") {
    LoadingOverlayPreviews()
}

#Preview("Dark") {
    LoadingOverlayPreviews()
        .preferredColorScheme(.dark)
}
