import SwiftUI
import ThemeUI

struct EmptyContent: View {
    
    @EnvironmentObject
    private var theme: AppTheme
    
    private let title: String
    private let message: String
    private let imageResource: ImageResource?

    init(title: String, message: String, icon: ImageResource? = nil) {
        self.title = title
        self.message = message
        self.imageResource = icon
    }

    var body: some View {
        VStack {
            if let icon = imageResource {
                Image(icon)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 64, height: 64)
                    .foregroundStyle(theme.colorScheme.primary)
                Spacer().frame(height: 16)
            }
            Text(title)
                .font(theme.typography.titleLarge)
            Spacer().frame(height: 8)
            Text(message)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 24)
                    
        }.padding(.horizontal, 24)
    }
}

#Preview {
    TeeTimeCaddieTheme {
        EmptyContent(title: "No Content", message: "There is no content. Add some content and it will show here.", icon: .teeEmpty)
    }
}
