import Foundation
import SwiftUI
import ThemeUI

enum ButtonType {
    case Filled
    case Outlined
    case Text
}

enum ButtonColor {
    case Primary
    case Secondary
    case Tertiary
    
    func themeColor(from theme: AppTheme) -> Color {
        switch self {
            case .Primary:
                return theme.colorScheme.primary
            case .Secondary:
                return theme.colorScheme.secondary
            case .Tertiary:
                return theme.colorScheme.tertiary
        }
    }
}


fileprivate struct TtcButtonStyle: ViewModifier {
    @EnvironmentObject
    private var theme: AppTheme
            
    private let type: ButtonType
    private let color: ButtonColor
    
    init(type: ButtonType, color: ButtonColor) {
        self.color = color
        self.type = type
    }
    
    private var themeColor: Color {
        color.themeColor(from: theme)
    }
    
    func body(content: Content) -> some View {
        switch type {
            case .Filled:
                let colors = theme.colorScheme.deaultButtonColors.copy(backgroundColor: themeColor, foregroundColor: theme.colorScheme.contentColorFor(themeColor))
                content
                    .buttonStyle(FilledButtonStyle(colors))
            case .Outlined:
                let colors = theme.colorScheme.outlinedButtonColors.copy(foregroundColor: themeColor, disabledForegroundColor: themeColor.opacity(0.5))
                content
                    .buttonStyle(OutlinedButtonStyle(colors))
            case .Text:
                let colors = theme.colorScheme.textButtonColors.copy(foregroundColor: themeColor, disabledForegroundColor: themeColor.opacity(0.5))
                content
                    .buttonStyle(TextButtonStyle(colors))
        }
    }
}

extension View {
    func buttonStyle(_ type: ButtonType, color: ButtonColor = .Primary) -> some View {
        self.modifier(TtcButtonStyle(type: type, color: color))
    }
}
