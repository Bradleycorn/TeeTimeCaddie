import Foundation
import SwiftUI
import TeeTimeCaddieKit

struct TeeTimesList: View {
    private let teeTimes: [TeeTime]
    private let onItemTapped: (TeeTime)->Void

    init(_ list: [TeeTime], onItemTapped: @escaping (TeeTime)->Void) {
        self.teeTimes = list
        self.onItemTapped = onItemTapped
    }

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 8) {
                ForEach(teeTimes, id: \.id) { teeTime in
                    TeeTimeRow(teeTime: teeTime)
                        .onTapGesture {
                            onItemTapped(teeTime)
                        }
                }
            }
            .padding()
        }
    }
}
