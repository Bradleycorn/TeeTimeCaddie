import Foundation
import SwiftUI
import ThemeUI
import TeeTimeCaddieKit


struct TeeTimeCard: View {

    @EnvironmentObject
    private var theme: AppTheme

    private let teeTime: TeeTime
    
    init(for teeTime: TeeTime) {
        self.teeTime = teeTime
    }
    
    var body: some View {
        Surface(color: theme.colorScheme.primaryContainer) {
            HStack(alignment: .center, spacing: 0) {
                Text(teeTime.shortDate)
                    .multilineTextAlignment(.center)
                    .padding(8)
                    .frame(maxHeight: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/)
                    .background(theme.colorScheme.primary)
                    .foregroundColor(theme.colorScheme.onPrimary)
                VStack(alignment: .leading, spacing: 0) {
                    Text(teeTime.course)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                .padding(8)
            }
        }
        .fixedSize(horizontal: false, vertical: /*@START_MENU_TOKEN@*/true/*@END_MENU_TOKEN@*/)
        .clipShape(theme.shapes.large)
    }
}

#Preview {
    TeeTimeCaddieTheme {
        VStack {
            TeeTimeCard(for: TeeTimeKt.previewTeeTime)
                .padding(.horizontal, 16)
        }
    }
}
