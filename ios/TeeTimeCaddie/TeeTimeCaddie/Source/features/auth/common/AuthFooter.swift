import SwiftUI
import ThemeUI

struct AuthFooter: View {
    @EnvironmentObject
    private var theme: AppTheme
    
    private let text: String
    private let actionText: String?
    private let onActionClick: ()->Void

    init(
        _ text: String,
        actionText: String?,
        onActionClick: @escaping () -> Void = {}) {
            
        self.text = text
        self.actionText = actionText
        self.onActionClick = onActionClick
    }


    var body: some View {
        HStack {
            Text(text)
            
            if let label = actionText {
                Button(label, action: onActionClick)
                    .buttonStyle(.Text)
                    .bold()                
            }
        }
    }
}

struct AuthFooter_Previews: PreviewProvider {
    static var previews: some View {
        TeeTimeCaddieTheme {
            AuthFooter("Do something else?", actionText: "Click Here")
        }
    }
}
