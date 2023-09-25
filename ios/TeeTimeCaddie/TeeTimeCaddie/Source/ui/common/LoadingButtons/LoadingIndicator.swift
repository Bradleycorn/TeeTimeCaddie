import SwiftUI

fileprivate let DEFAULT_DOT_SIZE: CGFloat = 12

struct LoadingIndicator: View {
    private let numberOfDots = 3
    private let indicatorType: DotType
    private let dotSize: CGFloat
    
    init(_ type: DotType = .Flashing, dotSize: CGFloat = DEFAULT_DOT_SIZE) {
        self.indicatorType = type
        self.dotSize = dotSize
    }
    
    var body: some View {
        AnimatedDots(numberOfDots: numberOfDots, type: indicatorType, size: dotSize)
    }
}

struct LoadingIndicator_Previews: PreviewProvider {
    static var previews: some View {
        VStack {
            Spacer()
            LoadingIndicator(.Flashing, dotSize: 12)
            Spacer()
            LoadingIndicator(.Pulsing, dotSize: 20)
                .foregroundColor(.blue)
            Spacer()
            LoadingIndicator(.Bouncing, dotSize: 12)
                .foregroundColor(.red)
            Spacer()
        }
    }
}
