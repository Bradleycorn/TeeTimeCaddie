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
        EmptyView()
    }
}
