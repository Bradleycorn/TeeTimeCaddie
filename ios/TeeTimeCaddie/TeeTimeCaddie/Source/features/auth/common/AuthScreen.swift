import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

struct AuthScreen<Content: View>: View {
    @EnvironmentObject
    private var theme: AppTheme
    
    private let analyticsScreen: AnalyticsScreen
    private let title: String
    private let footerText: String?
    private let footerActionText: String?
    private let onFooterActionClick: ()->Void
    private let screenContent: Content
    
    init(analyticsScreen: AnalyticsScreen,
         title: String,
         footerText: String? = nil,
         footerActionText: String? = nil,
         onFooterActionClick: @escaping () -> Void = {},
         @ViewBuilder screenContent: ()->Content) {
        
        self.analyticsScreen = analyticsScreen
        self.title = title
        self.footerText = footerText
        self.footerActionText = footerActionText
        self.onFooterActionClick = onFooterActionClick
        self.screenContent = screenContent()
    }
    
    
    var body: some View {
        Screen(analyticsScreen) {
            VStack(alignment: .center,spacing: 0) {
                AuthHeader(title)
                
                screenContent
                
                
                if let footer = footerText {
                    Spacer()
                    
                    AuthFooter(footer, actionText: footerActionText, onActionClick: onFooterActionClick)
                    
                    Spacer()
                        .frame(height: 24)

                }
            }
            .padding(.horizontal, 24)
            .background(theme.colorScheme.surfaceVariant)
            .foregroundStyle(theme.colorScheme.contentColorFor(theme.colorScheme.surfaceVariant))
        }
    }
}

struct AuthScreen_Previews: PreviewProvider {
    static var previews: some View {
        TeeTimeCaddieTheme {
            AuthScreen(
                analyticsScreen: AnalyticsScreen.Login.shared,
                title: "Auth Screen",
                footerText: "Do Something Else?",
                footerActionText: "Click Here") {
                    
                    Text("This is an Auth Screen")
                    
                }
        }
    }
}
