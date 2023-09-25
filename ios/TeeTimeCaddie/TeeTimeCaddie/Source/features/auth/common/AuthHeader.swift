//
//  AuthHeader.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 9/13/23.
//

import SwiftUI

struct AuthHeader: View {
    private let title: String
    
    init(_ title: String) {
        self.title = title
    }

    var body: some View {
        VStack(alignment: .center, spacing: 0) {
            Text(title)
                .font(.title)
                .bold()

            Spacer()
                .frame(height: 16)
            
            Image("icon")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(maxWidth: .infinity, maxHeight: 164)
                .foregroundColor(.blue)
            
            Spacer()
                .frame(height: 32)

        }
    }
}

struct AuthHeader_Previews: PreviewProvider {
    static var previews: some View {
        AuthHeader("Auth Header")
    }
}
