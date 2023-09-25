//
//  AuthFooter.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 9/13/23.
//

import SwiftUI

struct AuthFooter: View {
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
            }
        }
    }
}

struct AuthFooter_Previews: PreviewProvider {
    static var previews: some View {
        AuthFooter("Do something else?", actionText: "Click Here")
    }
}
