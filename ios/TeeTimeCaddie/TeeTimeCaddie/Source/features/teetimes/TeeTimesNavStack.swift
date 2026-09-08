import SwiftUI

struct TeeTimesNavStack: View {
    private let navigator: Navigator
    
    init(navigator: Navigator) {
        self.navigator = navigator
    }
    
    var body: some View {
        NavigationStack(path: navigator.backstack(for: .teeTimes)) {
            AppTabs.teeTimes.destinationView(navigator)
                .navigationDestination(for: AnyTtcNavKey.self) { key in
                    switch key.wrapped {
                    default: fatalError("Unhandled navigation key: \(key)")
                    }
                }
        }
    }
}
