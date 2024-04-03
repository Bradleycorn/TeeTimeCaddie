import SwiftUI
import ThemeUI

struct AuthHeader: View {
    @EnvironmentObject
    private var theme: AppTheme
    
    private let title: String
    
    init(_ title: String) {
        self.title = title
    }

    var body: some View {
        VStack(alignment: .center, spacing: 0) {
            Text(title)
                .font(theme.typography.headlineLarge)

            Spacer()
                .frame(height: 16)
            
            Image("icon")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(maxWidth: .infinity, maxHeight: 164)
                .foregroundColor(theme.colorScheme.primary)
            
            Spacer()
                .frame(height: 32)

        }
    }
}

struct AuthHeader_Previews: PreviewProvider {
    static var previews: some View {
        TeeTimeCaddieTheme {
            AuthHeader("Auth Header")
        }
    }
}
