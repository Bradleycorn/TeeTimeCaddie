import Foundation
import SwiftUI

public struct FilledButtonStyle: ButtonStyle {
    @Environment(\.isEnabled)
    private var isEnabled: Bool
    
    @EnvironmentObject
    private var theme: AppTheme
    
    private let customColors: ButtonColors?
    private let shape: AnyShape
    
    public init(_ colors: ButtonColors? = nil, shape: some Shape = ButtonDefaults.shape) {
        self.customColors = colors
        self.shape = AnyShape(shape)
    }
    
    private var colors: ButtonColors {
        customColors ?? ButtonDefaults.colors(from: theme.colorScheme)
    }
            
    public func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .modifier(ThemedButtonStyle(padding: ButtonDefaults.padding, colors: colors, shape: shape))
    }
}

public struct OutlinedButtonStyle: ButtonStyle {
    @Environment(\.isEnabled)
    private var isEnabled: Bool
    
    @EnvironmentObject
    private var theme: AppTheme
    
    private let customColors: ButtonColors?
    private let shape: AnyShape
    
    public init(_ colors: ButtonColors? = nil, shape: some Shape = ButtonDefaults.shape) {
        self.customColors = colors
        self.shape = AnyShape(shape)
    }
        
    private var colors: ButtonColors {
        customColors ?? ButtonDefaults.outlinedColors(from: theme.colorScheme)
    }
    
    private var foregroundColor: Color {
        (isEnabled) ? colors.foregroundColor : colors.disabledForegroundColor
    }
    
    public func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .modifier(ThemedButtonStyle(padding: ButtonDefaults.padding, colors: colors, shape: shape))
            .overlay(
                shape
                    .stroke(foregroundColor, lineWidth: ButtonDefaults.outlinedButtonBorderWidth)
            )
    }
}

public struct TextButtonStyle: ButtonStyle {
    @Environment(\.isEnabled)
    private var isEnabled: Bool
    
    @EnvironmentObject
    private var theme: AppTheme
    
    private let customColors: ButtonColors?
    
    public init(_ colors: ButtonColors? = nil, shape: some Shape = ButtonDefaults.shape) {
        self.customColors = colors
    }
        
    private var colors: ButtonColors {
        customColors ?? ButtonDefaults.outlinedColors(from: theme.colorScheme)
    }
    
    private var foregroundColor: Color {
        (isEnabled) ? colors.foregroundColor : colors.disabledForegroundColor
    }
    
    public func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .modifier(ThemedButtonStyle(padding: EdgeInsets(), colors: colors, shape: AnyShape(Rectangle())))
            .bold()
    }
}


fileprivate struct ThemedButtonStyle: ViewModifier {
    @Environment(\.isEnabled)
    private var isEnabled: Bool

    var padding: EdgeInsets
    var colors: ButtonColors
    var shape: AnyShape
    
    private var backgroundColor: Color {
        (isEnabled) ? colors.backgroundColor : colors.disabledBackgroundColor
    }
    
    private var foregroundColor: Color {
        (isEnabled) ? colors.foregroundColor : colors.disabledForegroundColor
    }
    
    func body(content: Content) -> some View {
        content
            .padding(padding)
            .background(backgroundColor)
            .foregroundStyle(foregroundColor)
            .clipShape(shape)
    }
}

public struct ButtonColors {
    let backgroundColor: Color
    let foregroundColor: Color
    let disabledBackgroundColor: Color
    let disabledForegroundColor: Color
    
    public func copy(
        backgroundColor: Color? = nil,
        foregroundColor: Color? = nil,
        disabledBackgroundColor: Color? = nil,
        disabledForegroundColor: Color? = nil) -> ButtonColors {
            
            ButtonColors(
                backgroundColor: backgroundColor ?? self.backgroundColor,
                foregroundColor: foregroundColor ?? self.foregroundColor,
                disabledBackgroundColor: disabledBackgroundColor ?? self.disabledBackgroundColor,
                disabledForegroundColor: disabledForegroundColor ?? self.disabledForegroundColor)
        }
}

extension Colors {    
    public var deaultButtonColors: ButtonColors {
        ButtonColors(
            backgroundColor: primary,
            foregroundColor: onPrimary,
            disabledBackgroundColor: primaryContainer,
            disabledForegroundColor: onPrimaryContainer.opacity(0.35))
    }
    
    public var outlinedButtonColors: ButtonColors {
        ButtonColors(
            backgroundColor: .clear,
            foregroundColor: primary,
            disabledBackgroundColor: .clear,
            disabledForegroundColor: primary.opacity(0.5))
    }
    
    public var textButtonColors: ButtonColors{
        ButtonColors(
            backgroundColor: .clear,
            foregroundColor: primary,
            disabledBackgroundColor: .clear,
            disabledForegroundColor: primary.opacity(0.5)
        )
    }
}

#Preview {
    DefaultTheme {
        VStack {
            Button("Filled Enabled", action: /*@START_MENU_TOKEN@*/{}/*@END_MENU_TOKEN@*/)
                .buttonStyle(FilledButtonStyle())
            
            Button("Filled Disabled", action: {})
                .buttonStyle(FilledButtonStyle())
                .disabled(true)
            
            Button("Outlined Enabled", action: /*@START_MENU_TOKEN@*/{}/*@END_MENU_TOKEN@*/)
                .buttonStyle(OutlinedButtonStyle())
            
            Button("Outlined Disabled", action: {})
                .buttonStyle(OutlinedButtonStyle())
                .disabled(true)

            Button("Text Enabled", action: /*@START_MENU_TOKEN@*/{}/*@END_MENU_TOKEN@*/)
                .buttonStyle(TextButtonStyle())
            
            Button("Text Disabled", action: {})
                .buttonStyle(TextButtonStyle())
                .disabled(true)
        }
    }
}
