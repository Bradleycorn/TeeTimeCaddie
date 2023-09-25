//
//  FirebaseEventPlugin.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 5/21/23.
//

import Foundation
import TeeTimeCaddieKit
import FirebaseAnalytics

class FirebaseEventPlugin: EventPlugin {
    func logEvent(event: AnalyticsEvent) -> Bool {
        Analytics.logEvent(event.name, parameters: event.asMap)
        return true
    }
    
    func logScreenView(screen: AnalyticsScreen, displayMethod: ScreenType) -> Bool {

        
        var params: [String:Any] = screen.parameters
        
        params[AnalyticsParameterScreenName] = screen.name
        params[AnalyticsParameterScreenClass] = displayMethod.displayName
        
        Analytics.logEvent(AnalyticsEventScreenView, parameters: params)

        return true
    }
    
    func setUserId(userId: String) {
        Analytics.setUserID(userId)
    }
}
