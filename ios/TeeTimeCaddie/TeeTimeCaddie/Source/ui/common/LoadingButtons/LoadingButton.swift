import SwiftUI

struct LoadingButton<Label: View>: View {
    private let type: DotType
    private let isLoading: Bool
    private let action: ()->Void
    private let labelContent: Label

    init(type: DotType = .Flashing, isLoading: Bool, action: @escaping () -> Void, @ViewBuilder label: ()->Label) {
        self.type = type
        self.isLoading = isLoading
        self.action = action
        self.labelContent = label()
    }
    
    var body: some View {
        Button(action: { action() }) {
            labelContent
                .loadingOverlay(type: type, isLoading: isLoading)
        }
    }
}

extension LoadingButton where Label == Text {
    init(_ title: String, type: DotType = .Flashing, isLoading: Bool, action: @escaping () -> Void) {
        self.init(type: type, isLoading: isLoading, action: action) {
            Text(title)
        }
    }
}


struct LoadingButton_Previews: PreviewProvider {

    struct Content: View {
        @State  var defaultLoading: Bool = false
        @State  var pulsingLoading: Bool = false
        @State  var bouncingLoading: Bool = false
        
        var body: some View {
            VStack {
                Spacer()
                LoadingButton("Flashing Loading Button", isLoading: defaultLoading, action: { defaultLoading.toggle()})
                    .buttonStyle(.Outlined)
                Spacer()
                LoadingButton("Pulsing Loading Button", type: .Pulsing, isLoading: pulsingLoading, action: { pulsingLoading.toggle() })
                    .buttonStyle(.Filled, color: .Secondary)
                Spacer()
                LoadingButton("Bouncing Loading Button", type: .Bouncing, isLoading: bouncingLoading, action: { bouncingLoading.toggle() })
                    .buttonStyle(.Outlined, color: .Tertiary)
                Spacer()
            }
        }

    }
    
    static var previews: some View {
        TeeTimeCaddieTheme {
            Content()
        }
    }
}
