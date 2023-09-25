//
//  FormStack.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 9/14/23.
//

import SwiftUI

struct FormStack<FormFields: View>: View {
    
    private let formFields: FormFields

    init(@ViewBuilder formFields: ()->FormFields) {
        self.formFields = formFields()
    }

    var body: some View {
        VStack(spacing: 0) {
            formFields
        }
        .background(Color.white)
        .cornerRadius(12)
        
    }
}

struct FormStack_Previews: PreviewProvider {
    static var previews: some View {
        ZStack {
            Color(UIColor.systemGroupedBackground)
            
            FormStack {
                TextField("Text Field", text: .constant(""))
                    .formFieldPadding()

                FormFieldDivider()

                PasswordField("Password Field", text: .constant(""))
                    .formFieldPadding()
            }.padding(.horizontal, 24)
        }
    }
}
