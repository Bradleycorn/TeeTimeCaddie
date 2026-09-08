import ThemeUI
import SwiftUI

struct ContentLoadingIndicator: View {
    @EnvironmentObject
    private var theme: AppTheme

    var body: some View {
        ProgressView()
            .progressViewStyle(.circular)
            .tint(theme.colorScheme.primary)
            .controlSize(.large)
    }
}

#Preview {
    TeeTimeCaddieTheme {
        ContentLoadingIndicator()
    }
}
