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
    
    func logScreenView(screenName: String, extras: [String : String]) -> Bool {
        // Handled by Firebase SwiftUI Extensions
        return true
    }
    
    func setUserId(userId: String) {
        Analytics.setUserID(userId)
    }
}
