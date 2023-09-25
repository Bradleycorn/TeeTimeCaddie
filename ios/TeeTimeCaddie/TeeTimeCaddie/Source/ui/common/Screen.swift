//
//  Screen.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 9/11/23.
//

import SwiftUI
import TeeTimeCaddieKit

struct Screen<Content: View>: View {
    
    let screen: AnalyticsScreen
    let screenContent: Content
    
    init(_ screen: AnalyticsScreen, @ViewBuilder content: () -> Content) {
        self.screen = screen
        self.screenContent = content()
    }
    
    var body: some View {
        screenContent
            .onAppear {
                AppModule.shared.eventManager().logScreenView(screen: screen, type: .screen)
            }
    }
}
