//
//  TextField.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 3/26/24.
//

import Foundation
import SwiftUI
import ThemeUI

struct TtcTextField: View {
    @Binding private var value: String
    private let label: String
    private let placeholder: String
    
    init(_ label: String, value: Binding<String>, placeholder: String = "") {
        self._value = value
        self.label = label
        self.placeholder = placeholder
    }
        
    var body: some View {
        FormField(label) {
            TextField(placeholder, text: $value)
        }
    }
}

#Preview {
    TeeTimeCaddieTheme {
        VStack(spacing: 16) {
            TtcTextField("Field Name", value: .constant(""), placeholder: "placeholder text")
            DateField("Some Date", selectedDate: .constant(nil))
            TtcTextField("Field Name", value: .constant(""), placeholder: "placeholder text")
            TtcTextField("Field Name", value: .constant(""), placeholder: "placeholder text")
        }.padding(16)
    }
}

struct FormField<Content: View>: View {
    @EnvironmentObject private var theme: AppTheme
    @FocusState private var isFocused: Bool
    
    private let label: String
    
    private let content: ()->Content
    
    init(_ label: String, @ViewBuilder content: @escaping ()->Content) {
        self.label = label
        self.content = content
    }
    
    private var outlineColor: Color {
        (isFocused) ? theme.colorScheme.primary : theme.colorScheme.outline
    }
    
    private var labelColor: Color {
        (isFocused) ? theme.colorScheme.primary : theme.colorScheme.onSurface
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(label)
                .foregroundStyle(labelColor)
                .font(theme.typography.labelMedium)
                .padding(.leading, 8)
                .padding(.bottom, 2)
            content()
                .focused($isFocused)
                .padding(EdgeInsets(horizontal: 8, vertical: 12))
                .overlay(
                    theme.shapes.small
                        .stroke(outlineColor, lineWidth: 1)
                )
        }
    }
}
            
